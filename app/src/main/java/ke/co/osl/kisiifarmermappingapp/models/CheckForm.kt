package ke.co.osl.kisiifarmermappingapp.models

data class CheckForm(
    val Data: Data,
    val FarmerDetails: Int,
    val FarmerAddress:Int,
    val  FarmerGroups:Int,
    val FarmerResources:Int,
    val  FarmerValueChains:Int
)

data class Data(
    val Name: String,
    val NationalID: String,
    val Phone: String,
    val User: String
)