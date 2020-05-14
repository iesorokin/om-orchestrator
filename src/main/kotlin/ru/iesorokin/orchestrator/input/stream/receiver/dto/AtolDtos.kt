package ru.iesorokin.orchestrator.input.stream.receiver.dto


data class AtolTransactionMessage(val atolId: String,
                                  val status: String,
                                  val fiscalData: AtolTransactionFiscalData,
                                  val processInstanceId: String? = null,
                                  val processId: String? = null,//todo: delete after 01.04.2020 - useless variable
                                  val correlationKey: String? = null)

data class AtolTransactionFiscalData(val uuid: String,
                                     val ecrRegistrationNumber: String,
                                     val fiscalDocumentNumber: Long,
                                     val fiscalStorageNumber: String)

enum class AtolTransactionStatusType { DONE }
