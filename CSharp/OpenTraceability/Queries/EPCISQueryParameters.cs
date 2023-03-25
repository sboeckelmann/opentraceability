﻿using System;
using System.Collections.Generic;
using System.Reflection;
using System.Text;
using System.Web;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;

namespace OpenTraceability.Queries
{
    public enum EPCISQueryType
    {
        Unknown = 0,
        events = 1
    }

    public class EPCISQueryParameters
    {
        static Dictionary<string, PropertyInfo> _prop_mapping = new Dictionary<string, PropertyInfo>();

        static EPCISQueryParameters()
        {
            foreach (var prop in typeof(EPCISQuery).GetProperties())
            {
                _prop_mapping.Add(prop.Name, prop);
            }
        }

        public EPCISQueryParameters()
        {

        }

        /// <summary>
        /// Constructs the parameters from a list of one or more epcs and populates
        /// the MATCH_anyEPC and MATCH_anyEPCClass parameters.
        /// </summary>
        /// <param name="epcs"></param>
        public EPCISQueryParameters(params EPC[] epcs)
        {
            foreach (var epc in epcs)
            {
                if (epc.Type == EPCType.Class)
                {
                    if (query.MATCH_anyEPCClass == null)
                    {
                        query.MATCH_anyEPCClass = new List<string>();
                    }
                    query.MATCH_anyEPCClass.Add(epc.ToString());
                }
                else
                {
                    if (query.MATCH_anyEPC == null)
                    {
                        query.MATCH_anyEPC = new List<string>();
                    }
                    query.MATCH_anyEPC.Add(epc.ToString());
                }
            }
        }

        /// <summary>
        /// This will parse the query parameters from the URI and populate the the EPCISQueryParameters object.
        /// </summary>
        /// <param name="uri"></param>
        public EPCISQueryParameters(Uri uri)
        {
            // split into each query parameter
            foreach (var qp in uri.Query.Substring(1).Split('&'))
            {
                string key = qp.Split('=').First();
                string value = HttpUtility.UrlDecode(qp.Split('=').Last());

                if (_prop_mapping.ContainsKey(key))
                {
                    PropertyInfo prop = _prop_mapping[key];
                    if (prop != null)
                    {
                        if (prop.PropertyType == typeof(DateTime?))
                        {
                            DateTime dt = DateTime.Parse(value);
                            prop.SetValue(query, dt);
                        }
                        else if (prop.PropertyType == typeof(List<string>))
                        {
                            List<string> values = value.Split('|').ToList();
                            prop.SetValue(query, values);
                        }
                        else if (prop.PropertyType == typeof(List<Uri>))
                        {
                            List<Uri> values = value.Split('|').Select(u => new Uri(u)).ToList();
                            prop.SetValue(query, values);
                        }
                    }
                }
            }
        }

        public EPCISQueryType queryType { get; set; } = EPCISQueryType.events;

        public EPCISQuery query { get; set; } = new EPCISQuery();

        public bool IsValid(out string? error)
        {
            error = null;
            return true;
        }

        public string ToJSON()
        {
            string json = JsonConvert.SerializeObject(this.query, new JsonSerializerSettings()
            {
                Formatting = Formatting.Indented,
                DateFormatString = "o",
                NullValueHandling = NullValueHandling.Ignore
            });

            JObject jobject = new JObject();
            jobject["queryType"] = this.queryType.ToString();
            jobject["query"] = JObject.Parse(json);

            return jobject.ToString();
        }

        public string ToQueryParameters()
        {
            List<string> queryParameters = new List<string>();

            // go through each property on the on the query
            foreach (var prop in typeof(EPCISQuery).GetProperties())
            {
                if (prop.PropertyType == typeof(DateTime?))
                {
                    DateTime? dateTime = (DateTime?)prop.GetValue(query);
                    if (dateTime != null)
                    {
                        string queryParam = $"{prop.Name}={dateTime.Value.ToString("o")}";
                        queryParameters.Add(queryParam);
                    }
                }
                else if (prop.PropertyType == typeof(List<string>))
                {
                    List<string>? list = prop.GetValue(query) as List<string>;
                    if (list != null && list.Count > 0)
                    {
                        string queryParam = $"{prop.Name}={string.Join('|', list.Select(l => HttpUtility.UrlEncode(l)))}";
                        queryParameters.Add(queryParam);
                    }
                }
                else if (prop.PropertyType == typeof(List<Uri>))
                {
                    List<Uri>? list = prop.GetValue(query) as List<Uri>;
                    if (list != null && list.Count > 0)
                    {
                        string queryParam = $"{prop.Name}={string.Join('|', list.Select(l => HttpUtility.UrlEncode(l.ToString())))}";
                        queryParameters.Add(queryParam);
                    }
                }
            }

            string q = "?" + string.Join("&", queryParameters.ToArray());
            return q;
        }
    }

    public class EPCISQuery
    {
        public DateTime? GE_recordTime { get; set; }
        public DateTime? LE_recordTime { get; set; }
        public DateTime? GE_eventTime { get; set; }
        public DateTime? LE_eventTime { get; set; }
        public List<string>? eventTypes { get; set; }
        public List<string>? MATCH_epc { get; set; }
        public List<string>? MATCH_epcClass { get; set; }
        public List<string>? MATCH_anyEPC { get; set; }
        public List<string>? MATCH_anyEPCClass { get; set; }
        public List<string>? EQ_bizStep { get; set; }
        public List<Uri>? EQ_bizLocation { get; set; }
    }
}
