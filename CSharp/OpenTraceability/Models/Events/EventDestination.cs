﻿using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System;
using System.ComponentModel;
using System.Linq;

namespace OpenTraceability.Models.Events
{
    public enum EventDestinationType
    {
        Unknown = 0,

        [CBV("owning_party")]
        [CBV("https://ref.gs1.org/cbv/SDT-owning_party")]
        [CBV("urn:epcglobal:cbv:sdt:owning_party")]
        Owner = 1,

        [CBV("possessing_party")]
        [CBV("https://ref.gs1.org/cbv/SDT-possessing_party")]
        [CBV("urn:epcglobal:cbv:sdt:possessing_party")]
        Possessor = 2,

        [CBV("location")]
        [CBV("https://ref.gs1.org/cbv/SDT-location")]
        [CBV("urn:epcglobal:cbv:sdt:location")]
        Location = 3
    }

    public class EventDestination
    {
        [OpenTraceabilityJson("type")]
        [OpenTraceability("@type")]
        public Uri Type { get; set; }

        public EventDestinationType ParsedType
        {
            get 
            {
                EventDestinationType type = EventDestinationType.Unknown;

                foreach (var e in Enum.GetValues(typeof(EventDestinationType)))
                {
                    if (EnumUtil.GetEnumAttributes<CBVAttribute>(e).Exists(attribute => attribute.Value.ToLower() == Type?.ToString().ToLower()))
                    {
                        return (EventDestinationType)e;
                    }
                }

                return type;
            }
            set
            {
                string t = EnumUtil.GetEnumAttributes<CBVAttribute>(value).Where(e => e.Value.StartsWith("urn")).FirstOrDefault()?.Value;
                if (!string.IsNullOrWhiteSpace(t))
                {
                    this.Type = new Uri(t);
                }
            }
        }

        [OpenTraceabilityJson("destination")]
        [OpenTraceability("text()")]
        public string Value { get; set; }

        public EventDestination()
        {

        }
    }
}