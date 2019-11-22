package org.kethereum.contract.abi.types.model.types

import org.kethereum.contract.abi.types.PaginatedByteArray
import org.kethereum.contract.abi.types.model.ETHType
import org.kethereum.contract.abi.types.model.ETH_TYPE_PAGESIZE
import org.kethereum.contract.abi.types.rightPadToFixedPageSize
import org.walleth.khex.hexToByteArray

class DynamicSizedBytesETHType(override val paddedValue: ByteArray) : ETHType<ByteArray> {

    override fun toKotlinType(): ByteArray {
        val input = PaginatedByteArray(paddedValue)
        val len = UIntETHType.ofPaginatedByteArray(input, "256")
        return input.getBytes(len!!.toKotlinType().intValueExact())
    }

    companion object {
        fun ofPaginatedByteArray(input: PaginatedByteArray): DynamicSizedBytesETHType? {
            val pos = UIntETHType.ofPaginatedByteArray(input, "256")?.toKotlinType() ?: return null
            input.jumpTo(pos.intValueExact())
            val len = UIntETHType.ofPaginatedByteArray(input, "256") ?: return null

            val array = len.toPaged().content + input.getBytes(len.toKotlinType().intValueExact()).rightPadToFixedPageSize(ETH_TYPE_PAGESIZE)
            input.endJump()
            return DynamicSizedBytesETHType(array)
        }

        fun ofNativeKotlinType(input: ByteArray) = DynamicSizedBytesETHType(UIntETHType.ofNativeKotlinType(input.size.toBigInteger(), "256").paddedValue + input.rightPadToFixedPageSize(ETH_TYPE_PAGESIZE))

        fun ofString(input: String) = ofNativeKotlinType(input.hexToByteArray())
    }

    override fun isDynamic() = true

}
