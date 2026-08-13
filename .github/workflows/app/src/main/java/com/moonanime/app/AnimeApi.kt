package com.moonanime.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class AniListApi {

    private val client = OkHttpClient()

    private val endpoint =
        "https://graphql.anilist.co"

    suspend fun trending(): List<Anime> {
        return queryAnime(
            """
            query {
                Page(perPage: 20) {
                    media(
                        type: ANIME
                        sort: TRENDING_DESC
                    ) {
                        id
                        title {
                            romaji
                            english
                        }
                        description
                        episodes
                        status
                        format
                        genres
                        averageScore
                        coverImage {
                            large
                        }
                    }
                }
            }
            """
        )
    }

    suspend fun search(
        text: String
    ): List<Anime> {

        return queryAnime(
            """
            query {
                Page(perPage: 30) {
                    media(
                        type: ANIME
                        search: "${escape(text)}"
                        sort: SEARCH_MATCH
                    ) {
                        id
                        title {
                            romaji
                            english
                        }
                        description
                        episodes
                        status
                        format
                        genres
                        averageScore
                        coverImage {
                            large
                        }
                    }
                }
            }
            """
        )
    }

    suspend fun getAnime(
        id: Int
    ): AnimeDetails {

        return withContext(Dispatchers.IO) {

            val query =
                """
                query {
                    Media(id: $id, type: ANIME) {

                        id

                        title {
                            romaji
                            english
                        }

                        description
                        episodes
                        status
                        format
                        genres
                        averageScore

                        coverImage {
                            large
                        }

                        relations {
                            edges {

                                relationType(version: 2)

                                node {
                                    id

                                    title {
                                        romaji
                                        english
                                    }

                                    format
                                    episodes

                                    coverImage {
                                        large
                                    }

                                    averageScore
                                }
                            }
                        }

                        recommendations(
                            sort: RATING_DESC
                            perPage: 15
                        ) {

                            nodes {

                                rating

                                mediaRecommendation {

                                    id

                                    title {
                                        romaji
                                        english
                                    }

                                    format
                                    episodes

                                    coverImage {
                                        large
                                    }

                                    averageScore
                                }
                            }
                        }
                    }
                }
                """

            val json =
                execute(query)

            parseDetails(
                json
                    .getJSONObject("data")
                    .getJSONObject("Media")
            )
        }
    }

    private suspend fun queryAnime(
        query: String
    ): List<Anime> {

        return withContext(Dispatchers.IO) {

            val json =
                execute(query)

            val media =
                json
                    .getJSONObject("data")
                    .getJSONObject("Page")
                    .getJSONArray("media")

            parseAnimeList(media)
        }
    }

    private fun execute(
        query: String
    ): JSONObject {

        val body =
            JSONObject()
                .put("query", query)
                .toString()

        val request =
            Request.Builder()
                .url(endpoint)
                .post(
                    body.toRequestBody(
                        "application/json"
                            .toMediaType()
                    )
                )
                .build()

        client.newCall(request)
            .execute()
            .use { response ->

                if (!response.isSuccessful) {

                    throw Exception(
                        "AniList request failed: ${response.code}"
                    )
                }

                return JSONObject(
                    response.body
                        ?.string()
                        ?: throw Exception(
                            "Empty AniList response"
                        )
                )
            }
    }

    private fun parseAnimeList(
        array: JSONArray
    ): List<Anime> {

        val result =
            mutableListOf<Anime>()

        for (i in 0 until array.length()) {

            result += parseAnime(
                array.getJSONObject(i)
            )
        }

        return result
    }

    private fun parseAnime(
        item: JSONObject
    ): Anime {

        val title =
            item.getJSONObject("title")

        val english =
            title.optString("english")

        val romaji =
            title.optString("romaji")

        val genres =
            mutableListOf<String>()

        val genreArray =
            item.optJSONArray("genres")

        if (genreArray != null) {

            for (i in
                0 until genreArray.length()
            ) {

                genres +=
                    genreArray.getString(i)
            }
        }

        return Anime(

            id =
                item.getInt("id"),

            title =
                if (english.isNotBlank())
                    english
                else
                    romaji,

            description =
                item.optString(
                    "description"
                ),

            episodes =
                if (item.isNull("episodes"))
                    null
                else
                    item.optInt(
                        "episodes"
                    ),

            status =
                item.optString("status"),

            format =
                item.optString("format"),

            genres =
                genres,

            imageUrl =
                item
                    .optJSONObject(
                        "coverImage"
                    )
                    ?.optString("large")
                    ?: "",

            score =
                item.optDouble(
                    "averageScore",
                    0.0
                )
        )
    }

    private fun parseDetails(
        item: JSONObject
    ): AnimeDetails {

        val anime =
            parseAnime(item)

        val relations =
            mutableListOf<AnimeRelation>()

        val relationEdges =
            item
                .getJSONObject("relations")
                .getJSONArray("edges")

        for (i in
            0 until relationEdges.length()
        ) {

            val edge =
                relationEdges.getJSONObject(i)

            val node =
                edge.getJSONObject("node")

            relations += AnimeRelation(

                type =
                    edge.optString(
                        "relationType"
                    ),

                anime =
                    parseAnime(node)
            )
        }

        val recommendations =
            mutableListOf<AnimeRecommendation>()

        val recommendationNodes =
            item
                .getJSONObject(
                    "recommendations"
                )
                .getJSONArray("nodes")

        for (i in
            0 until recommendationNodes.length()
        ) {

            val node =
                recommendationNodes
                    .getJSONObject(i)

            val media =
                node.optJSONObject(
                    "mediaRecommendation"
                )

            if (media != null) {

                recommendations +=
                    AnimeRecommendation(

                        rating =
                            node.optInt(
                                "rating",
                                0
                            ),

                        anime =
                            parseAnime(media)
                    )
            }
        }

        return AnimeDetails(

            anime = anime,

            relations = relations,

            recommendations =
                recommendations
        )
    }

    private fun escape(
        text: String
    ): String {

        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", " ")
    }
}

data class AnimeDetails(

    val anime: Anime,

    val relations:
        List<AnimeRelation>,

    val recommendations:
        List<AnimeRecommendation>
)

data class AnimeRelation(

    val type: String,

    val anime: Anime
)

data class AnimeRecommendation(

    val rating: Int,

    val anime: Anime
)