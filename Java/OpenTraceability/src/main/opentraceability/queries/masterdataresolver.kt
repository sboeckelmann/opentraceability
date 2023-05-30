package queries

import io.grpc.netty.shaded.io.netty.internal.tcnative.AsyncTask
import java.net.http.HttpClient
import models.identifiers.*
import models.events.*
import models.masterdata.Location
import models.masterdata.Tradeitem
import models.events.EPCISBaseDocument
import models.identifiers.GLN
import models.identifiers.GTIN
import models.identifiers.PGLN
import models.masterdata.TradingParty
import java.lang.reflect.Type
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers

class MasterDataResolver {
    companion object {
        suspend fun resolveMasterData(
            options: DigitalLinkQueryOptions,
            doc: EPCISBaseDocument,
            client: HttpClient
        ) {
            for (evt in doc.Events) {
                for (p in evt.Products) {
                    if (p.epc.type == EPCType.Class || p.epc.type == EPCType.Instance) {
                        resolveTradeitem(options, p.epc.gtin, doc, client)
                    }
                }

                resolveLocation(options, evt.location?.gln, doc, client)

                for (source in evt.sourceList) {
                    if (source.parsedType == EventSourceType.Owner) {
                        val pgln = PGLN(source.value ?: throw Exception("source in event source list has NULL value."))
                        if (pgln != null) {
                            resolveTradingParty(options, pgln, doc, client)
                        }
                    }
                }

                for (dest in evt.destinationList) {
                    if (dest.parsedType == EventDestinationType.Owner) {
                        val pgln = PGLN(dest.value ?: throw Exception("source in event source list has NULL value."))
                        if (pgln != null) {
                            resolveTradingParty(options, pgln, doc, client)
                        }
                    }
                }
            }
        }

        suspend inline fun <reified TTradeitem : Tradeitem, TLocation : Location, TTradingParty : TradingParty> resolveMasterData(
            options: DigitalLinkQueryOptions,
            doc: EPCISBaseDocument,
            client: HttpClient
        ) {
            for (evt in doc.events) {
                for (p in evt.products) {
                    if (p.epc.type == EPCType.Class || p.epc.type == EPCType.Instance) {
                        resolveTradeitem<TTradeitem>(options, p.epc.gtin, doc, client)
                    }
                }

                resolveLocation<TLocation>(options, evt.location?.gln, doc, client)

                for (source in evt.sourceList) {
                    if (source.type == OpenTraceability.Constants.EPCIS.URN.SDT_Owner) {
                        val pgln = PGLN(source.value ?: throw Exception("source in event source list has NULL value."))
                        if (pgln != null) {
                            resolveTradingParty<TTradingParty>(options, pgln, doc, client)
                        }
                    }
                }

                for (dest in evt.destinationList) {
                    if (dest.type == OpenTraceability.Constants.EPCIS.URN.SDT_Owner) {
                        val pgln = PGLN(dest.value ?: throw Exception("source in event source list has NULL value."))
                        if (pgln != null) {
                            resolveTradingParty<TTradingParty>(options, pgln, doc, client)
                        }
                    }
                }
            }
        }

        suspend fun resolveTradeitem(
            options: DigitalLinkQueryOptions,
            gtin: GTIN?,
            doc: EPCISBaseDocument,
            client: HttpClient
        ) {
            if (gtin != null) {
                if (doc.getMasterData<Tradeitem>(gtin.toString()) == null) {
                    val t = Setup.getMasterDataTypeDefault(Tradeitem::class.java) ?: Tradeitem::class.java
                    val ti = resolveMasterDataItem<Tradeitem>(t, options, "/01/$gtin?linkType=gs1:masterData", client)
                    if (ti != null) {
                        doc.masterData.add(ti)
                    }
                }
            }
        }

        suspend fun resolveLocation(
            options: DigitalLinkQueryOptions,
            gln: GLN?,
            doc: EPCISBaseDocument,
            client: HttpClient
        ) {
            if (gln != null) {
                if (doc.getMasterData<Location>(gln.toString()) == null) {
                    val t = Setup.getMasterDataTypeDefault(Location::class.java) ?: Location::class.java
                    val l = resolveMasterDataItem<Location>(t, options, "/414/$gln?linkType=gs1:masterData", client)
                    if (l != null) {
                        doc.masterData.add(l)
                    }
                }
            }
        }

        suspend fun resolveTradingParty(
            options: DigitalLinkQueryOptions,
            pgln: PGLN?,
            doc: EPCISBaseDocument,
            client: HttpClient
        ) {
            if (pgln != null) {
                if (doc.getMasterData<TradingParty>(pgln.toString()) == null) {
                    val t = Setup.getMasterDataTypeDefault(TradingParty::class.java) ?: TradingParty::class.java
                    val tp = resolveMasterDataItem<TradingParty>(t, options, "/417/$pgln?linkType=gs1:masterData", client)
                    if (tp != null) {
                        doc.masterData.add(tp)
                    }
                }
            }
        }

        suspend inline fun <reified T : Tradeitem> resolveTradeitem(
            options: DigitalLinkQueryOptions,
            gtin: GTIN?,
            doc: EPCISBaseDocument,
            client: HttpClient
        ) {
            if (gtin != null) {
                if (doc.getMasterData<Tradeitem>(gtin.toString()) == null) {
                    val ti = ResolverMasterDataItem<T>(options, "/01/$gtin?linkType=gs1:masterData", client)
                    if (ti != null) {
                        doc.masterData.add(ti)
                    }
                }
            }
        }

        suspend inline fun <reified T : Location> resolveLocation(
            options: DigitalLinkQueryOptions,
            gln: GLN?,
            doc: EPCISBaseDocument,
            client: HttpClient
        ) {
            if (gln != null) {
                if (doc.getMasterData<Location>(gln.toString()) == null) {
                    val l = ResolverMasterDataItem<T>(options, "/414/$gln?linkType=gs1:masterData", client)
                    if (l != null) {
                        doc.masterData.add(l)
                    }
                }
            }
        }

        suspend inline fun <reified T : TradingParty> resolveTradingParty(
            options: DigitalLinkQueryOptions,
            pgln: PGLN?,
            doc: EPCISBaseDocument,
            client: HttpClient
        ) {
            if (pgln != null) {
                if (doc.getMasterData<TradingParty>(pgln.toString()) == null) {
                    val tp = ResolverMasterDataItem<T>(options, "/417/$pgln?linkType=gs1:masterData", client)
                    if (tp != null) {
                        doc.masterData.add(tp)
                    }
                }
            }
        }

        suspend inline fun <reified T : IVocabularyElement> ResolverMasterDataItem(
            options: DigitalLinkQueryOptions,
            relativeURL: String,
            client: HttpClient
        ): T? {
            val response = resolveMasterDataItem(T::class.java, options, relativeURL, client)
            return response as? T
        }

        suspend fun resolveMasterDataItem(
            type: Class<*>,
            options: DigitalLinkQueryOptions,
            relativeURL: String,
            client: HttpClient
        ): Any? = withContext(Dispatchers.IO) {
            if (options.url == null) {
                throw Exception("options.Uri is null on the DigitalLinkQueryOptions")
            }

            val request = HttpRequest.newBuilder()
                .uri(URI(options.url.toString().trimEnd('/') + "/" + relativeURL.trimStart('/')))
                .GET()
                .build()

            val response = client.send(request, BodyHandlers.ofString())

            if (response.statusCode() == 200) {
                val responseStr = response.body()
                val links = mutableListOf<DigitalLink>()
                val json = JsonParser().parse(responseStr).asJsonArray
                for (element in json) {
                    val link = element as JsonObject
                    links.add(DigitalLink(link["link"].asString))
                }
                if (links.isNotEmpty()) {
                    for (link in links) {
                        try {
                            val itemResponse = client.send(
                                HttpRequest.newBuilder()
                                    .uri(URI(link.link))
                                    .GET()
                                    .build(),
                                BodyHandlers.ofString()
                            )

                            if (itemResponse.statusCode() == 200) {
                                val item = OpenTraceabilityMappers.MasterData.GS1WebVocab.Map(type, itemResponse.body())
                                if (item != null) {
                                    if (item.id == null) {
                                        throw Exception("While resolve a $type through the GS1 Digital Link Resolver, the $type returned " +
                                                "had an empty or invalid Identifier. The link that was resolved was $link and the results was ${itemResponse.body()}")
                                    } else {
                                        return@withContext item
                                    }
                                }
                            }
                        } catch (ex: Exception) {
                            println(ex)
                        }
                    }
                }
            } else {
                val contentStr = response.body()
                throw HttpRequestException("There was an error trying to fetch digital links from $relativeURL - $contentStr - ${response.toString()}")
            }

            null
        }
    }
}
