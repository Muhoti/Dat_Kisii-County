package ke.co.osl.kisiifarmermappingapp.models

data class ValueChainBody(
    val ValueChainID: String,
    val FarmerID: String,
    val Name: String,
    val Variety: String,
    val Unit: String,
    val ApproxAcreage: String,
    val AvgYearlyProduction: String,
    val AvgHarvestProduction: String
    )