package com.utb.wms.data.local

import androidx.room.TypeConverter
import com.utb.wms.domain.model.DocumentStatus
import com.utb.wms.domain.model.MovementType

class Converters {

    @TypeConverter
    fun fromMovementType(value: MovementType): String = value.name

    @TypeConverter
    fun toMovementType(value: String): MovementType = MovementType.valueOf(value)

    @TypeConverter
    fun fromDocumentStatus(value: DocumentStatus): String = value.name

    @TypeConverter
    fun toDocumentStatus(value: String): DocumentStatus = DocumentStatus.valueOf(value)
}
