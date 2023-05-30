package models.events

import interfaces.IEvent
import interfaces.IEventKDE
import java.util.*
import models.identifiers.*
import models.events.kdes.CertificationList
import models.events.*
import models.identifiers.EPC
import models.identifiers.PGLN
import utility.attributes.*
import utility.attributes.OpenTraceabilityXmlIgnoreAttribute
import java.time.Duration
import java.lang.reflect.Type
import java.net.URI
import java.time.OffsetDateTime

class AssociationEvent : EventBase(), IEvent {

    @OpenTraceabilityAttribute("","parentID", 7)
    var ParentID: EPC? = null

    @OpenTraceabilityProductsAttribute("childQuantityList", EPCISVersion.V2, EventProductType.Child, 9, OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute("childEPCs", EventProductType.Child, 8, OpenTraceabilityProductsListType.EPCList)
    var Children: ArrayList<EventProduct> = ArrayList<EventProduct>()

    @OpenTraceabilityAttribute("","action", 10)
    override  var Action: EventAction? = null

    @OpenTraceabilityAttribute("","bizStep", 11)
    override  var BusinessStep: URI? = null


    @OpenTraceabilityAttribute("","disposition", 12)
    override  var Disposition: URI? = null


    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","readPoint", 13)
    override  var ReadPoint: EventReadPoint = EventReadPoint()


    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","bizLocation", 14)
    override lateinit var Location: EventLocation

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("bizTransaction")
    @OpenTraceabilityAttribute("","bizTransactionList", 15, EPCISVersion.V2)
    override  var BizTransactionList: ArrayList<EventBusinessTransaction> = ArrayList<EventBusinessTransaction>()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("source")
    @OpenTraceabilityAttribute("","sourceList", 16, EPCISVersion.V2)
    override var SourceList: ArrayList<EventSource> = ArrayList<EventSource>()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("destination")
    @OpenTraceabilityAttribute("","destinationList", 17, EPCISVersion.V2)
    override var DestinationList: ArrayList<EventDestination> = ArrayList<EventDestination>()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("sensorElement")
    @OpenTraceabilityAttribute("","sensorElementList", 18, EPCISVersion.V2)
    override var SensorElementList: ArrayList<SensorElement> = ArrayList<SensorElement>()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","persistentDisposition", 19)
    override var PersistentDisposition: PersistentDisposition? = PersistentDisposition()

    var ILMD: EventILMD = EventILMD()

    @OpenTraceabilityXmlIgnoreAttribute
    @OpenTraceabilityAttribute("","type", 0)
    override lateinit var EventType: EventType



}
