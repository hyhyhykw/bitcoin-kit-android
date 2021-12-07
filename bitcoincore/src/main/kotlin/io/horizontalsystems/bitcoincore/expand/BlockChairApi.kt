package io.horizontalsystems.bitcoincore.expand

import android.text.TextUtils
import io.horizontalsystems.bitcoincore.core.IInitialSyncApi
import io.horizontalsystems.bitcoincore.managers.ApiManager
import io.horizontalsystems.bitcoincore.managers.TransactionItem
import io.horizontalsystems.bitcoincore.managers.TransactionOutputItem

/**
 * Created time : 2021/12/7 20:31.
 * @author 10585
 */

class BlockChairApi @JvmOverloads constructor(
    token: String,
    private val test: Boolean = false,
    log: Boolean = false
) : IInitialSyncApi {
    private val tokenQuery = if (TextUtils.isEmpty(token)) "" else "token=$token"

    private val apiManager = ApiManager("https://api.blockcypher.com/",log)

    private val limit = 50

    override fun getTransactions(addresses: List<String>): List<TransactionItem> {
        val chain = if (test) "test3" else "main"
        val path = "v1/btc/$chain/addrs/"
        val list = ArrayList<TransactionItem>()
        for (address in addresses) {
            fetchTransactions(path, address, list, "")
        }

        return list
    }

    private fun fetchTransactions(
        path: String,
        address: String,
        list: ArrayList<TransactionItem>,
        extraPath: String
    ) {
        val uri = "$path$address/full?$tokenQuery&limit=$limit$extraPath"
        val json = apiManager.doOkHttpGet(true, uri).asObject()

        val txs = json["txs"].asArray()

        var beforeHeight = -1
        for (i in 0 until txs.size()) {
            val tx = txs[i].asObject()

            val blockHash = tx["block_hash"].asString()
            val blockHeight = tx["block_height"].asInt()

            val outputs = tx.get("outputs").asArray()

            val map = outputs.filter { it.asObject()["addresses"] != null }
                .map {
                    val output = it.asObject()
                    val addresses = output["addresses"].asArray()

                    val script = output["script"].asString()
                    TransactionOutputItem(script, addresses[0].asString())
                }


            list.add(TransactionItem(blockHash, blockHeight, map))

            if (i == txs.size() - 1) {
                beforeHeight = blockHeight
            }
        }

        val hasMore = json.getBoolean("hasMore", false)
        if (hasMore) {
            fetchTransactions(path, address, list, "?before=$beforeHeight")
        }
    }
}