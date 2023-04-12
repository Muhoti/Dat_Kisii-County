package ke.co.osl.kisiifarmermappingapp.models

data class GetValueChainBody(
    val ValueChainID: String,
    val Name: String,
    val FarmerID: String,
    val Variety: String,
    val Unit: String,
    val ApproxAcreage: String,
    val AvgYearlyProduction: String,
    val AvgHarvestProduction: String,
    val updatedAt: String
)