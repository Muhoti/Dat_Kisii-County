package ke.co.osl.kisiifarmermappingapp.models

data class StatsStatus(
    val FarmerDetails: Res,
    val FarmerAddresses: Res,
    val FarmerGroups: Res,
    val FarmerResources: Res,
    val ValueChains: Res,
)

data class Res (
    val total: Int,
    val unique: Int,
)
