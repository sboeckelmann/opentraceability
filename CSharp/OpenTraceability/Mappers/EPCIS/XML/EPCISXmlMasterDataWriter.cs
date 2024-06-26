﻿using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Common;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Models.MasterData;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;
using System.Xml.XPath;

namespace OpenTraceability.Mappers.EPCIS.XML
{
    public class EPCISXmlMasterDataWriter
    {
        public static void WriteMasterData(XElement xDocument, EPCISBaseDocument doc)
        {
            if (doc.MasterData.Count > 0)
            {
                XElement xEPCISHeader = xDocument.Element("EPCISHeader");
                if (xEPCISHeader == null)
                {
                    xDocument.Add(new XElement("EPCISHeader", new XElement("extension", new XElement("EPCISMasterData", new XElement("VocabularyList")))));
                }
                else
                {
                    xEPCISHeader.Add(new XElement("extension", new XElement("EPCISMasterData", new XElement("VocabularyList"))));
                }
                XElement xVocabList = xDocument.XPathSelectElement("EPCISHeader/extension/EPCISMasterData/VocabularyList") ?? throw new Exception("Failed to grab the element EPCISHeader/extension/EPCISMasterData/VocabularyList.");

                foreach (var mdList in doc.MasterData.GroupBy(m => m.EPCISType))
                {
                    if (mdList.Key != null)
                    {
                        WriteMasterDataList(mdList.ToList(), xVocabList, mdList.Key);
                    }
                    else
                    {
                        throw new Exception("There are master data vocabulary elements where the Type is NULL.");
                    }
                }
            }
        }

        private static void WriteMasterDataList(List<IVocabularyElement> data, XElement xVocabList, string type)
        {
            if (data.Count > 0)
            {
                XElement xVocab = new XElement("Vocabulary", new XAttribute("type", type), new XElement("VocabularyElementList"));
                XElement xVocabEleList = xVocab.Element("VocabularyElementList") ?? throw new Exception("Failed to grab the element VocabularyElementList");

                foreach (IVocabularyElement md in data)
                {
                    XElement xMD = WriteMasterDataObject(md);
                    xVocabEleList.Add(xMD);
                }

                xVocabList.Add(xVocab);
            }
        }

        private static XElement WriteMasterDataObject(IVocabularyElement md)
        {
            XElement xVocabEle = new XElement("VocabularyElement");
            xVocabEle.Add(new XAttribute("id", md.ID ?? string.Empty));

            var mappings = OTMappingTypeInformation.GetMasterDataXmlTypeInfo(md.GetType());

            foreach (var mapping in mappings.Properties)
            {
                string id = mapping.Name;
                PropertyInfo p = mapping.Property;

                object o = p.GetValue(md);
                if (o != null)
                {
                    if (id == string.Empty)
                    {
                        var subMappings = OTMappingTypeInformation.GetMasterDataXmlTypeInfo(o.GetType());
                        foreach (var subMapping in subMappings.Properties)
                        {
                            string subID = subMapping.Name;
                            PropertyInfo subProperty = subMapping.Property;
                            object subObj = subProperty.GetValue(o);
                            if (subObj != null)
                            {
                                if (subObj.GetType() == typeof(List<LanguageString>))
                                {
                                    List<LanguageString> l = (List<LanguageString>)subObj;
                                    string str = l.FirstOrDefault()?.Value;
                                    if (str != null)
                                    {
                                        XElement xAtt = new XElement("attribute", new XAttribute("id", subID));
                                        xAtt.Value = str;
                                        xVocabEle.Add(xAtt);
                                    }
                                }
                                else
                                {
                                    string str = subObj.ToString();
                                    if (str != null)
                                    {
                                        XElement xAtt = new XElement("attribute", new XAttribute("id", subID));
                                        xAtt.Value = str;
                                        xVocabEle.Add(xAtt);
                                    }
                                }
                            }
                        }
                    }
                    else if (p.GetCustomAttribute<OpenTraceabilityObjectAttribute>() != null)
                    {
                        XElement xAtt = new XElement("attribute", new XAttribute("id", id));
                        WriteObject(xAtt, p.PropertyType, o);
                        xVocabEle.Add(xAtt);
                    }
                    else if (p.GetCustomAttribute<OpenTraceabilityArrayAttribute>() != null)
                    {
                        IList l = (IList)o;
                        foreach (var i in l)
                        {
                            string str = i.ToString();
                            if (str != null)
                            {
                                XElement xAtt = new XElement("attribute", new XAttribute("id", id));
                                xAtt.Value = str;
                                xVocabEle.Add(xAtt);
                            }
                        }
                    }
                    else if (o.GetType() == typeof(List<LanguageString>))
                    {
                        List<LanguageString> l = (List<LanguageString>)o;
                        string str = l.FirstOrDefault()?.Value;
                        if (str != null)
                        {
                            XElement xAtt = new XElement("attribute", new XAttribute("id", id));
                            xAtt.Value = str;
                            xVocabEle.Add(xAtt);
                        }
                    }
                    else
                    {
                        string str = o.ToString();
                        if (str != null)
                        {
                            XElement xAtt = new XElement("attribute", new XAttribute("id", id));
                            xAtt.Value = str;
                            xVocabEle.Add(xAtt);
                        }
                    }
                }
            }

            foreach (IMasterDataKDE kde in md.KDEs)
            {
                XElement xKDE = kde.GetEPCISXml();
                if (xKDE != null)
                {
                    xVocabEle.Add(xKDE);
                }
            }

            return xVocabEle;
        }

        private static void WriteObject(XElement x, Type t, object o)
        {
            foreach (var property in t.GetProperties())
            {
                object value = property.GetValue(o);
                if (value != null)
                {
                    OpenTraceabilityAttribute xmlAtt = property.GetCustomAttribute<OpenTraceabilityAttribute>();
                    if (xmlAtt != null)
                    {
                        XElement xchild = new XElement(xmlAtt.Name);
                        if (property.GetCustomAttribute<OpenTraceabilityObjectAttribute>() != null)
                        {
                            WriteObject(xchild, property.PropertyType, value);
                        }
                        else
                        {
                            string str = value.ToString();
                            if (str != null)
                            {
                                xchild.Value = str;
                            }
                        }
                        x.Add(xchild);
                    }
                }
            }
        }
    }
}
