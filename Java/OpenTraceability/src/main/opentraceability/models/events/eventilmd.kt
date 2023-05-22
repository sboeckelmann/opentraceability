package models.events
import java.util.*
import models.events.kdes.CertificationList
import java.time.OffsetDateTime
class EventILMD {
    var ProductionMethodForFishAndSeafoodCode: String = String()
    var ItemExpirationDate: OffsetDateTime? = null
    var ProductionDate: OffsetDateTime? = null
    var CountryOfOrigin: List<Country> = ArrayList<Country>()
    var LotNumber: String = String()
    var CertificationList: CertificationList = CertificationList()
    var ExtensionKDEs: List<IEventKDE> = ArrayList<IEventKDE>()
    companion object{
    }
}
