package opentraceability.models.events.kdes;

import opentraceability.interfaces.IEventKDE;
import org.json.JSONObject;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import org.w3c.dom.Element;
import kotlin.reflect.Type;
import kotlin.reflect.full.KTypeImpl;
import kotlin.reflect.jvm.internal.impl.types.StarProjectionImplKt;

public class EventKDEDateTime extends EventKDEBase implements IEventKDE {
    public Type valueType = new KTypeImpl(StarProjectionImplKt.starProjectionType(), null, OffsetDateTime.class);

    public OffsetDateTime value = null;

    public EventKDEDateTime() {
    }

    public EventKDEDateTime(String ns, String name) {
        this.namespace = ns;
        this.name = name;
    }

    @Override
    public JSONObject getJson() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Element getXml() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void setFromJson(JSONObject json) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void setFromXml(Element xml) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String toString() {
        if (value != null) {
            return value.toString();
        } else {
            return "";
        }
    }
}