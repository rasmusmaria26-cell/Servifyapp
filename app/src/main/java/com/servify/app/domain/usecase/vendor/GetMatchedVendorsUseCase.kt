package com.servify.app.domain.usecase.vendor

import com.servify.app.data.model.Vendor
import com.servify.app.data.repository.VendorRepository
import javax.inject.Inject

class GetMatchedVendorsUseCase @Inject constructor(
    private val vendorRepository: VendorRepository
) {
    suspend operator fun invoke(
        serviceCategory: String
    ): Result<List<Vendor>> {
        return vendorRepository.getVendors(serviceCategory)
            .map { vendors ->
                vendors.sortedByDescending { it.rating }
            }
    }
}
