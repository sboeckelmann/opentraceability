﻿using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events.KDEs;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Models.MasterData
{
    public class TradingParty : IVocabularyElement
    {
        public string? ID { get => PGLN?.ToString(); }
        public string? Type { get; set; } = "urn:epcglobal:epcis:vtype:Party";
        public VocabularyType VocabularyType => VocabularyType.TradingParty;

        public PGLN? PGLN { get; set; }

        [OpenTraceability("urn:epcglobal:cbv:owning_Party", 1)]
        public PGLN? OwningParty { get; set; }

        [OpenTraceability("urn:epcglobal:cbv:mda#informationProvider", 2)]
        public PGLN? InformationProvider { get; set; }

        [OpenTraceability("https://gs1.org/cbv/cbvmda:certificationList", Events.EPCISVersion.V1)]
        public CertificationList? CertificationList { get; set; }

        /// <summary>
        /// These are additional KDEs that were not mapped into the object.
        /// </summary>
        public List<IMasterDataKDE> KDEs { get; set; } = new List<IMasterDataKDE>();
    }
}