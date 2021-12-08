package io.horizontalsystems.bitcoincore.expand

/**
 * Created time : 2021/12/7 20:32.
 * @author 10585
 */
class BlockChairApiProvider(
    private val token: String,
    private val testApi: Boolean = false,
    private val log: Boolean = false
) :
    SyncApiProvider<BlockChairApi> {

    override val provider: BlockChairApi
        get() = BlockChairApi(token, testApi, log)
}