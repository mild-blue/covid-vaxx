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

        var exportedPatientsInfoSuccess: Int = 0
        var exportedPatientsInfoErrors: Int = 0

        var exportedVaccinationsSuccess: Int = 0
        var exportedVaccinationsErrors: Int = 0

        val patients = patientService.getPatientsByConjunctionOf(
            n = isinJobDto.patientsCount,
            offset = isinJobDto.patientsOffset.toLong()
        )

        logger.debug("${patients.count()} patients will be processed.")

        for (patient in patients) {
            logger.debug("Checking ISIN id of patient ${patient.id}")

            // 1. If patient has personal number but ISIN id is not set -> try ISIN validation
            val updatedPatient = if (!patient.isinId.isNullOrBlank()) {
                logger.info("ISIN id of patient ${patient.id} is already set, skipping ISIN validation.")
                patient
            } else if (isinJobDto.validatePatients && !patient.personalNumber.isNullOrBlank() ) {
                logger.info("Patient ${patient.id} has personal number but no ISIN id. Validating in ISIN.")

                val newIsinPatientId = retryPatientValidation(patient)

                logger.info(
                    "Patient ${patient.id} was validated in ISIN with " +
                    "obtained ISIN id: ${newIsinPatientId}."
                )

                if (newIsinPatientId != null) {
                    validatedPatientsSuccess++;
                } else {
                    validatedPatientsErrors++;
                }
                patient.copy(isinId = newIsinPatientId)
            } else {
                logger.info("ISIN id of patient ${patient.id} is not set but validatePatients=false. Skipping ISIN validation.")
                patient.copy(isinId = null)
            }

            if (updatedPatient.isinId.isNullOrBlank()) continue

            // 2. If data are correct but not exported to ISIN -> try export to isin
            logger.debug("Checking correctness exported to ISIN of patient ${updatedPatient.id}")
            if (
                isinJobDto.exportPatientsInfo &&
                updatedPatient.dataCorrect != null &&
                updatedPatient.dataCorrect.dataAreCorrect &&
                updatedPatient.dataCorrect.exportedToIsinOn == null
            ) {
                logger.debug("Retrying to export contact info of patient ${updatedPatient.id} to ISIN")
                val wasExported = retryPatientContactInfoExport(updatedPatient)

                logger.info("Patient ${patient.id} was exported to ISIN with result: ${wasExported}")

                if (wasExported) {
                    exportedPatientsInfoSuccess++
                } else {
                    exportedPatientsInfoErrors++
                }
            }

            // 3. If vaccinated but not vaccination is not exported to ISIN -> try export vaccination to isin
            if (isinJobDto.exportVaccinations && updatedPatient.vaccinated != null && updatedPatient.vaccinated.exportedToIsinOn == null) {
                logger.debug("Retrying to create vaccination of patient ${updatedPatient.id} in ISIN")
                val wasCreated = retryPatientVaccinationCreation(updatedPatient)

                logger.info("Patient ${patient.id} vaccination was created in ISIN with result: ${wasCreated}")

                if (wasCreated) {
                    exportedVaccinationsSuccess++
                } else {
                    exportedVaccinationsErrors++
                }
            }
        }

        return IsinJobDtoOut(
            validatedPatientsSuccess = validatedPatientsSuccess,
            validatedPatientsErrors = validatedPatientsErrors,
            exportedPatientsInfoSuccess = exportedPatientsInfoSuccess,
            exportedPatientsInfoErrors = exportedPatientsInfoErrors,
            exportedVaccinationsSuccess = exportedVaccinationsSuccess,
            exportedVaccinationsErrors = exportedVaccinationsErrors
        )
    }

    private suspend fun retryPatientValidation(patient: PatientDtoOut): String? {
        val patientValidationResult = patientValidation.validatePatient(
            firstName = patient.firstName,
            lastName = patient.lastName,
            personalNumber = patient.personalNumber ?: throw AssertionError { "Personal number cannot be null."},
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
            logger.debug { "Updating ISIN id of patient ${patient.id} to $newIsinPatientId"}
            patientRepository.updatePatientChangeSet(id = patient.id, isinId = newIsinPatientId.trim())
        } else {
            logger.debug { "NOT updating ISIN id of patient ${patient.id}"}
        }
        return newIsinPatientId
    }

    private suspend fun retryPatientContactInfoExport(patient: PatientDtoOut): Boolean {
        requireNotNull(patient.dataCorrect) { "Data correctness of patient ${patient.id} cannot be null." }

        val wasExported = isinService.tryExportPatientContactInfo(patient, notes= patient.dataCorrect.notes)

        if (wasExported) {
            logger.debug { "Updating exported to ISIN of correctness ${patient.dataCorrect.id} (patient ${patient.id})"}
            dataCorrectnessService.exportedToIsin(patient.dataCorrect.id)
        } else {
            logger.debug { "NOT updating exported to ISIN of correctness ${patient.dataCorrect.id} (patient ${patient.id})"}
        }
        return wasExported
    }

    private suspend fun retryPatientVaccinationCreation(patient: PatientDtoOut): Boolean {
        requireNotNull(patient.vaccinated) { "Vaccination of patient ${patient.id} cannot be null." }

        val vaccination = vaccinationService.get(patient.vaccinated.id)

        // Try to export vaccination to ISIN
        val wasExported = isinService.tryCreateVaccinationAndDose(
            vaccination.toPatientVaccinationDetailDto(),
            patient = patient
        )

        if (wasExported) {
            logger.debug { "Updating exported to ISIN of vaccination ${patient.vaccinated.id} (patient ${patient.id})"}
            vaccinationService.exportedToIsin(patient.vaccinated.id)
        } else {
            logger.debug { "NOT updating exported to ISIN of vaccination ${patient.vaccinated.id} (patient ${patient.id})"}
        }
        return wasExported
    }
}
