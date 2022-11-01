package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.repository.PatientRepository
import blue.mild.covid.vaxx.dto.internal.PatientValidationResult
import blue.mild.covid.vaxx.dto.request.IsinJobDtoIn
import blue.mild.covid.vaxx.dto.response.IsinJobDtoOut
import blue.mild.covid.vaxx.dto.response.PatientDtoOut
import blue.mild.covid.vaxx.dto.response.toPatientVaccinationDetailDto
import mu.KLogging

class IsinRetryService(
    private val patientService: PatientService,
    private val patientValidation: PatientValidationService,
    private val isinService: IsinServiceInterface,
    private val dataCorrectnessService: DataCorrectnessService,
    private val patientRepository: PatientRepository,
    private val vaccinationService: VaccinationService,
) {

    private companion object : KLogging()

    @Suppress("ComplexCondition", "ComplexMethod", "LongMethod") // this is complex job, it is ok here
    suspend fun runIsinRetry(isinJobDto: IsinJobDtoIn): IsinJobDtoOut {
        var validatedPatientsSuccess: Int = 0
        var validatedPatientsErrors: Int = 0

        var checkedVaccinationsSuccess: Int = 0
        var checkedVaccinationsErrors: Int = 0

        var exportedPatientsInfoSuccess: Int = 0
        var exportedPatientsInfoErrors: Int = 0

        var exportedVaccinationsFirstDoseSuccess: Int = 0
        var exportedVaccinationsFirstDoseErrors: Int = 0

        var exportedVaccinationsSecondDoseSuccess: Int = 0
        var exportedVaccinationsSecondDoseErrors: Int = 0

        val patients = patientService.getPatientsByConjunctionOf(
            n = isinJobDto.patientsCount,
            offset = isinJobDto.patientsOffset.toLong()
        )

        logger.info("${patients.count()} patients will be processed.")

        for (patient in patients) {
            logger.info("ISIN retry for patient ${patient.id}")

            // 1. If patient ISIN id is not set -> try ISIN validation
            val updatedPatient = if (!patient.isinId.isNullOrBlank()) {
                logger.info("ISIN id of patient ${patient.id} is already set, skipping ISIN validation.")
                patient
            } else if (isinJobDto.validatePatients) {
                logger.info("Patient ${patient.id} has no ISIN id. Validating in ISIN.")

                val newIsinPatientId = retryPatientValidation(patient)

                logger.info(
                    "Patient ${patient.id} was validated in ISIN with " +
                    "obtained ISIN id: $newIsinPatientId."
                )

                if (newIsinPatientId != null) {
                    validatedPatientsSuccess++
                } else {
                    validatedPatientsErrors++
                }
                patient.copy(isinId = newIsinPatientId)
            } else {
                logger.info("ISIN id of patient ${patient.id} is not set but validatePatients=false. Skipping ISIN validation.")
                patient.copy(isinId = null)
            }

            if (updatedPatient.isinId.isNullOrBlank()) continue

//            // Uncomment to cancel all vaccinations for this patient.
//            // This is used in testing to clean the test environment.
//             if (updatedPatient.isinId == "3646928931")
//                 isinService.cancelAllVaccinations(updatedPatient.isinId)
//             continue

            // 2. If isinReady is not set -> try to check vaccinations in ISIN
            if (
                isinJobDto.checkVaccinations &&
                updatedPatient.isinReady == null
            ) {
                logger.debug("Retrying to check ISIN vaccinations of patient ${updatedPatient.id} in ISIN")
                val wasChecked = retryPatientIsReadyForVaccination(updatedPatient)

                logger.info("Vaccinations of patient ${patient.id} was checked in ISIN with result: $wasChecked")

                if (wasChecked) {
                    checkedVaccinationsSuccess++
                } else {
                    checkedVaccinationsErrors++
                }
            }

            // 3. If data are correct but not exported to ISIN -> try export to isin
            logger.info("Checking correctness exported to ISIN of patient ${updatedPatient.id}")
            if (
                isinJobDto.exportPatientsInfo &&
                updatedPatient.dataCorrect != null &&
                updatedPatient.dataCorrect.dataAreCorrect &&
                updatedPatient.dataCorrect.exportedToIsinOn == null
            ) {
                logger.debug("Retrying to export contact info of patient ${updatedPatient.id} to ISIN")
                val wasExported = retryPatientContactInfoExport(updatedPatient)

                logger.info("Patient ${patient.id} was exported to ISIN with result: $wasExported")

                if (wasExported) {
                    exportedPatientsInfoSuccess++
                } else {
                    exportedPatientsInfoErrors++
                }
            }

            // 4. If vaccinated but vaccination is not exported to ISIN -> try export vaccination to ISIN
            if (isinJobDto.exportVaccinationsFirstDose && updatedPatient.vaccinated != null && updatedPatient.vaccinated.exportedToIsinOn == null) {
                logger.info("Retrying to create vaccination of patient ${updatedPatient.id} in ISIN")
                val wasCreated = retryPatientVaccinationFirstDoseCreation(updatedPatient)

                logger.info("Patient ${patient.id} vaccination was created in ISIN with result: $wasCreated")

                if (wasCreated) {
                    exportedVaccinationsFirstDoseSuccess++
                } else {
                    exportedVaccinationsFirstDoseErrors++
                }
            }

            // 5. If has second dose but it is not exported to ISIN -> try export second dose to ISIN
            if (
                isinJobDto.exportVaccinationsSecondDose &&
                updatedPatient.vaccinatedSecondDose != null &&
                updatedPatient.vaccinatedSecondDose.exportedToIsinOn == null
            ) {
                logger.info("Retrying to create second dose of patient ${updatedPatient.id} in ISIN")
                val wasCreated = retryPatientVaccinationSecondDoseCreation(updatedPatient)

                logger.info("Patient ${patient.id} 2nd dose vaccination was created in ISIN with result: $wasCreated")

                if (wasCreated) {
                    exportedVaccinationsSecondDoseSuccess++
                } else {
                    exportedVaccinationsSecondDoseErrors++
                }
            }
        }

        return IsinJobDtoOut(
            validatedPatientsSuccess = validatedPatientsSuccess,
            validatedPatientsErrors = validatedPatientsErrors,
            checkedVaccinationsSuccess = checkedVaccinationsSuccess,
            checkedVaccinationsErrors = checkedVaccinationsErrors,
            exportedPatientsInfoSuccess = exportedPatientsInfoSuccess,
            exportedPatientsInfoErrors = exportedPatientsInfoErrors,
            exportedVaccinationsFirstDoseSuccess = exportedVaccinationsFirstDoseSuccess,
            exportedVaccinationsFirstDoseErrors = exportedVaccinationsFirstDoseErrors,
            exportedVaccinationsSecondDoseSuccess = exportedVaccinationsSecondDoseSuccess,
            exportedVaccinationsSecondDoseErrors = exportedVaccinationsSecondDoseErrors
        )
    }

    private suspend fun retryPatientValidation(patient: PatientDtoOut): String? {
        val patientValidationResult = patientValidation.validatePatient(
            firstName = patient.firstName,
            lastName = patient.lastName,
            personalNumber = patient.personalNumber,
            insuranceNumber = patient.insuranceNumber
        )

        logger.info {
            "Validation of patient ${patient.firstName}/${patient.lastName}/${patient.personalNumber} " +
            "completed: status=${patientValidationResult.status}, isinPatientId=${patientValidationResult.patientId}."
        }

        val newIsinPatientId = when (patientValidationResult.status) {
            PatientValidationResult.PATIENT_FOUND ->
                patientValidationResult.patientId
            else -> {
                null
            }
        }

        if (newIsinPatientId != null) {
            logger.debug { "Updating ISIN id of patient ${patient.id} to $newIsinPatientId" }
            patientRepository.updatePatientChangeSet(id = patient.id, isinId = newIsinPatientId.trim())
        } else {
            logger.debug { "NOT updating ISIN id of patient ${patient.id}" }
        }
        return newIsinPatientId
    }

    private suspend fun retryPatientIsReadyForVaccination(patient: PatientDtoOut): Boolean {
        requireNotNull(patient.isinId) { "ISIN id of patient ${patient.id} cannot be null." }

        val isinReady = isinService.tryPatientIsReadyForVaccination(patient.isinId)

        return if (isinReady != null) {
            logger.debug { "Updating ISIN ready of patient ${patient.id} to value $isinReady" }
            patientService.updateIsinReady(patient.id, isinReady)
            true
        } else {
            logger.debug { "NOT updating ISIN ready of patient ${patient.id}" }
            false
        }
    }

    private suspend fun retryPatientContactInfoExport(patient: PatientDtoOut): Boolean {
        requireNotNull(patient.dataCorrect) { "Data correctness of patient ${patient.id} cannot be null." }

        val wasExported = isinService.tryExportPatientContactInfo(patient, notes = patient.dataCorrect.notes)

        if (wasExported) {
            logger.debug { "Updating exported to ISIN of correctness ${patient.dataCorrect.id} (patient ${patient.id})" }
            dataCorrectnessService.exportedToIsin(patient.dataCorrect.id)
        } else {
            logger.debug { "NOT updating exported to ISIN of correctness ${patient.dataCorrect.id} (patient ${patient.id})" }
        }
        return wasExported
    }

    private suspend fun retryPatientVaccinationFirstDoseCreation(patient: PatientDtoOut): Boolean {
        requireNotNull(patient.vaccinated) { "Vaccination of patient ${patient.id} cannot be null." }

        val vaccination = vaccinationService.get(patient.vaccinated.id)

        // Try to export vaccination to ISIN
        val wasExported = isinService.tryCreateVaccination(
            vaccination.toPatientVaccinationDetailDto(),
            patient = patient
        )

        if (wasExported) {
            logger.debug { "Updating exported to ISIN of vaccination ${patient.vaccinated.id} (patient ${patient.id})" }
            vaccinationService.exportedToIsin(patient.vaccinated.id)
        } else {
            logger.debug { "NOT updating exported to ISIN of vaccination ${patient.vaccinated.id} (patient ${patient.id})" }
        }
        return wasExported
    }

    private suspend fun retryPatientVaccinationSecondDoseCreation(patient: PatientDtoOut): Boolean {
        requireNotNull(patient.vaccinatedSecondDose) { "Vaccination 2nd dose of patient ${patient.id} cannot be null." }

        val vaccination = vaccinationService.get(patient.vaccinatedSecondDose.id)

        // Try to export vaccination to ISIN
        val wasExported = isinService.tryCreateVaccination(
            vaccination.toPatientVaccinationDetailDto(),
            patient = patient
        )

        if (wasExported) {
            logger.debug { "Updating exported to ISIN of vaccination ${patient.vaccinatedSecondDose.id} (patient ${patient.id})" }
            vaccinationService.exportedToIsin(patient.vaccinatedSecondDose.id)
        } else {
            logger.debug { "NOT updating exported to ISIN of vaccination ${patient.vaccinatedSecondDose.id} (patient ${patient.id})" }
        }
        return wasExported
    }
}
