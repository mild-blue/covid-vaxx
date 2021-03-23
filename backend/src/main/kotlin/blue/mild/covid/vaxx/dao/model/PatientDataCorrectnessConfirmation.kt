package blue.mild.covid.vaxx.dao.model

object PatientDataCorrectnessConfirmation : ManagedTable("patient_data_correctness") {
    /**
     * Patient data that were checked.
     */
    val patientId = patientReference().uniqueIndex()

    /**
     * What user performed check.
     */
    val userPerformedCheck = userReference("user_performed_check")

    /**
     * Nurse set during the login.
     */
    val nurseId = nurseReference().nullable()

    /**
     * Indication that the data are correct.
     */
    val dataAreCorrect = bool("data_are_correct")

    /**
     * Optional notes for the data check.
     */
    val notes = text("notes").nullable()
}
