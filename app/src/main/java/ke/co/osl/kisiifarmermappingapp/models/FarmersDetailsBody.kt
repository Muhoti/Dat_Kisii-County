package ke.co.osl.kisiifarmermappingapp.models

data class FarmersDetailsBody(
    val User: String,
    val NationalID: String,
    val Name: String,
    val Phone: String,
    val Gender: String,
    val AgeGroup: String,
    val FarmingType: String
)