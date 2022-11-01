package blue.mild.covid.vaxx.service

/**
 * This is code we could potentially use in VaccinationSlotService.
 * in order to perform transactions without locking.
 */
//
//

//    suspend fun selectAndBookForPatient(patientId: EntityId): VaccinationSlotDtoOut {
//        val patientIdName = VaccinationSlots.patientId.nameInDatabaseCase()
//        val tableName = VaccinationSlots.nameInDatabaseCase()
//        val idName = VaccinationSlots.id.nameInDatabaseCase()
//        val fromName = VaccinationSlots.from.nameInDatabaseCase()
//        // do not use suspend transaction for this
//        return newSuspendedTransaction {
//            """
//            update "$tableName"
//            set "$patientIdName" = '$patientId'
//            where "$idName" = (
//                select s."$idName" from "$tableName" s
//                where s."$patientIdName" is null
//                order by "$fromName"
//                limit 1
//            )
//            and $patientIdName is null
//            """.trimIndent().let { TransactionManager.current().exec(it) }
//
//            getAndMap({ VaccinationSlots.patientId eq patientId }, 1).singleOrNull()
//        } ?: throw NoVaccinationSlotsFoundException()
//    }

//        return newSuspendedTransaction {
//            VaccinationSlots
//                .select { VaccinationSlots.patientId.isNull() }
//                .orderBy(VaccinationSlots.from)
//                .limit(1)
//                .forUpdate()
//                .singleOrNull()
//                ?.let {
//                    it[VaccinationSlots.id]
//                }
//                ?.let { slotId ->
//                    VaccinationSlots.update(
//                        where = { VaccinationSlots.id eq slotId and VaccinationSlots.patientId.isNull() },
//                        body = { it[VaccinationSlots.patientId] = patientId }
//                    ).takeIf { it == 1 }
//
//                    VaccinationSlots.select {
//                        VaccinationSlots.id eq slotId
//                    }.notForUpdate()
//                        .singleOrNull()
//                        ?.mapVaccinationSlot()
//                        ?.takeIf { it.patientId == patientId }
//                }
//        } ?: throw NoVaccinationSlotsFoundException()
//    }
