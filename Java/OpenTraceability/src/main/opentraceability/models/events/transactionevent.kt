package models.events
import java.util.*
import models.identifiers.*
import models.events.kdes.CertificationList
import models.events.*
import java.time.Duration
import java.lang.reflect.Type
import java.net.URI
import java.time.OffsetDateTime
class TransactionEvent {
    var ParentID: EPC = EPC()
    var ReferenceProducts: List<EventProduct> = ArrayList<EventProduct>()
    var Action: EventAction? = null
    var BusinessStep: URI? = URI?()
    var Disposition: URI? = URI?()
    var ReadPoint: EventReadPoint = EventReadPoint()
    var Location: EventLocation = EventLocation()
    var BizTransactionList: List<EventBusinessTransaction> = ArrayList<EventBusinessTransaction>()
    var SourceList: List<EventSource> = ArrayList<EventSource>()
    var DestinationList: List<EventDestination> = ArrayList<EventDestination>()
    var SensorElementList: List<SensorElement> = ArrayList<SensorElement>()
    var PersistentDisposition: PersistentDisposition = PersistentDisposition()
    var ILMD: EventILMD = EventILMD()
    var EventType: EventType = EventType()
    var Products: List<EventProduct> = ArrayList<EventProduct>()
    var EventTime: OffsetDateTime? = null
    var RecordTime: OffsetDateTime? = null
    var EventTimeZoneOffset: Duration? = null
    var EventID: URI? = URI?()
    var ErrorDeclaration: ErrorDeclaration = ErrorDeclaration()
    var CertificationInfo: String = String()
    var CertificationList: CertificationList = CertificationList()
    var InformationProvider: PGLN = PGLN()
    var KDEs: List<IEventKDE> = ArrayList<IEventKDE>()
    companion object{
    }
}
