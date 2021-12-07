package io.horizontalsystems.bitcoincore.expand

import io.horizontalsystems.bitcoincore.core.IInitialSyncApi

/**
 * Created time : 2021/12/7 20:24.
 * @author 10585
 */
interface SyncApiProvider<out PROVIDER : IInitialSyncApi> {

    val provider: PROVIDER
}