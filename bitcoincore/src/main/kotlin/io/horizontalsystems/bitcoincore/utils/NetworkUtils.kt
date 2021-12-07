package io.horizontalsystems.bitcoincore.utils

import android.annotation.SuppressLint
import android.util.Log
import io.horizontalsystems.bitcoincore.extensions.toHexString
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.net.*
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import javax.net.ssl.*

object NetworkUtils {

    fun getLocalInetAddress(): InetAddress {
        try {
            return InetAddress.getLocalHost()
        } catch (e: UnknownHostException) {
            throw RuntimeException(e)
        }
    }

    fun getIPv6(inetAddr: InetAddress): ByteArray {
        val ip = inetAddr.address
        if (ip.size == 16) {
            return ip
        }

        if (ip.size == 4) {
            val ipv6 = ByteArray(16)
            ipv6[10] = -1
            ipv6[11] = -1
            System.arraycopy(ip, 0, ipv6, 12, 4)
            return ipv6
        }

        throw RuntimeException("Bad IP: " + ip.toHexString())
    }

    fun createSocket(): Socket {

        val socksProxyHost = System.getProperty("socksProxyHost")
        val socksProxyPort = System.getProperty("socksProxyPort")?.toIntOrNull()

        return if (socksProxyHost != null && socksProxyPort != null) {
            val socketAddress = InetSocketAddress.createUnresolved(socksProxyHost, socksProxyPort)
            val proxy = Proxy(Proxy.Type.SOCKS, socketAddress)
            Socket(proxy)
        } else {
            Socket()
        }
    }

    @SuppressLint("TrustAllX509TrustManager", "BadHostnameVerifier")
    fun getUnsafeOkHttpClient(log:Boolean=false): OkHttpClient {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(
                    object : X509TrustManager {
                        @Throws(CertificateException::class)
                        override fun checkClientTrusted(chain: Array<X509Certificate>,
                                                        authType: String) {
                        }

                        @Throws(CertificateException::class)
                        override fun checkServerTrusted(chain: Array<X509Certificate>,
                                                        authType: String) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> {
                            return arrayOf()
                        }
                    }
            )
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory
            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, (trustAllCerts[0] as X509TrustManager))
            builder.hostnameVerifier(HostnameVerifier { _, _ -> true })
            builder.connectTimeout(5000, TimeUnit.MILLISECONDS)
            builder.readTimeout(60000, TimeUnit.MILLISECONDS)
            if (log){
                builder.addInterceptor(HttpLoggingInterceptor(object :HttpLoggingInterceptor.Logger{
                    override fun log(message: String) {
                        Log.e("[OKHttp]",message)
                    }
                }).setLevel(HttpLoggingInterceptor.Level.BODY))
            }
            builder.build()

        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

}
