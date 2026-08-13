package com.moonanime.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AniListApi {

    private val client = OkHttpClient()

    private val endpoint =
        "https://graphql.anilist.co"

    suspend fun trending(): List<Anime> =
        queryAnime(
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

    suspend fun search(
        text: String
    ): List<Anime> =
        queryAnime(
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

    private suspend fun queryAnime(
        query: String
    ): List<Anime> =
        withContext(Dispatchers.IO) {

            val body = JSONObject()
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
                            "AniList error: ${response.code}"
                        )
                    }

                    val json =
                        JSONObject(
                            response.body
                                ?.string()
                                ?: throw Exception(
                                    "Empty response"
                                )
                        )

                    parse(
                        json
                            .getJSONObject("data")
                            .getJSONObject("Page")
                            .getJSONArray("media")
                    )
                }
        }

    private fun parse(
        array: org.json.JSONArray
    ): List<Anime> {

        val result =
            mutableListOf<Anime>()

        for (i in 0 until array.length()) {

            val item =
                array.getJSONObject(i)

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

                for (g in
                    0 until genreArray.length()
                ) {

                    genres +=
                        genreArray.getString(g)
                }
            }

            val cover =
                item
                    .optJSONObject("coverImage")
                    ?.optString("large")
                    ?: ""

            result += Anime(

                id = item.getInt("id"),

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
                    if (
                        item.isNull("episodes")
                    )
                        null
                    else
                        item.optInt(
                            "episodes"
                        ),

                status =
                    item.optString(
                        "status"
                    ),

                format =
                    item.optString(
                        "format"
                    ),

                genres = genres,

                imageUrl = cover,

                score =
                    item.optDouble(
                        "averageScore",
                        0.0
                    )
            )
        }

        return result
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