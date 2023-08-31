package com.example.marvel

import android.content.Context
import androidx.room.Room
import com.example.marvel.data.datasources.local.CharacterDao
import com.example.marvel.data.datasources.local.MarvelDatabase
import com.example.marvel.data.datasources.remote.MarvelApi
import com.example.marvel.data.repositories.MarvelRepositoryImpl
import com.example.marvel.domain.repositories.MarvelRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideOkHttpClient() : OkHttpClient{
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient().newBuilder()
            .addInterceptor { chain ->
                val currentTimestamp = System.currentTimeMillis()
                val newUrl = chain.request().url
                    .newBuilder()
                    .addQueryParameter("ts", currentTimestamp.toString())
                    .addQueryParameter("apikey", "bab1bc3b0a42fbec137e1947094f3d08")
                    .addQueryParameter("hash",
                        provideToMd5Hash(currentTimestamp.toString()
                                + "e095911e313417dd344e207dda7b791a3f8bdf90" + "bab1bc3b0a42fbec137e1947094f3d08"))
                    .build()

                val newRequest = chain.request()
                    .newBuilder()
                    .url(newUrl)
                    .build()
                chain.proceed(newRequest)
            }
            .addInterceptor(logging)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(client: OkHttpClient): Retrofit{ //essa funcao tem uma depencia do okhttp cliente, pra ela funcionar, tem que passar primeiro pela okhttpclient
        return Retrofit.Builder()
            .baseUrl("https://gateway.marvel.com/v1/public/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Singleton
    @Provides
    fun provideServiceApi(retrofit: Retrofit): MarvelApi{
        return retrofit.create(MarvelApi::class.java)
    }

    private fun provideToMd5Hash(encrypted: String): String {
        var pass = encrypted
        var encryptedString: String? = null
        val md5: MessageDigest
        try {
            md5 = MessageDigest.getInstance("MD5")
            md5.update(pass.toByteArray(), 0, pass.length)
            pass = BigInteger(1, md5.digest()).toString(16)
            while (pass.length < 32) {
                pass = "0$pass"
            }
            encryptedString = pass
        } catch (e1: NoSuchAlgorithmException) {
            e1.printStackTrace()
        }
        return encryptedString ?: ""
    }
    @Provides
    @Singleton
    fun provideMarvelDatabase(@ApplicationContext context: Context): MarvelDatabase =
        Room.databaseBuilder(context, MarvelDatabase::class.java, "marvel_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideCharacterDao(marvelDatabase: MarvelDatabase): CharacterDao =
        marvelDatabase.characterDao()

    @Provides
    @Singleton
    fun provideMarvelRepository(marvelApi: MarvelApi, characterDao: CharacterDao): MarvelRepository {
        return MarvelRepositoryImpl(marvelApi, characterDao)
    }
}