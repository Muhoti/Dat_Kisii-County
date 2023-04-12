package ke.co.osl.kisiifarmermappingapp.models

data class UpdateProduceBody(
    val ValueChainID: String,
    val ValueChain: String,
    val FarmerID: String,
    val Produce: String,
    val HarvestDate: String,
    val FarmingPeriod: String
)