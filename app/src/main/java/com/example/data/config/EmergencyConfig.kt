package com.example.data.config

/**
 * الأرقام من بيانات وزارة الصحة المصرية، نوفمبر 2026. راجعها قبل أي إطلاق.
 */
data class EmergencyContactNumber(
    val id: String,
    val label: String,
    val number: String,
    val isPrimaryAmbulance: Boolean = false
)

object EmergencyConfig {
    const val COUNTRY = "EG"

    val contacts: List<EmergencyContactNumber> = listOf(
        EmergencyContactNumber(
            id = "ambulance",
            label = "الإسعاف: 123",
            number = "123",
            isPrimaryAmbulance = true
        ),
        EmergencyContactNumber(
            id = "mental_health_hotline",
            label = "الخط الساخن للصحة النفسية: 16328",
            number = "16328",
            isPrimaryAmbulance = false
        ),
        EmergencyContactNumber(
            id = "addiction_hotline",
            label = "خط علاج الإدمان: 16023",
            number = "16023",
            isPrimaryAmbulance = false
        ),
        EmergencyContactNumber(
            id = "mental_health_secretariat",
            label = "الأمانة العامة للصحة النفسية وعلاج الإدمان: 0220816831",
            number = "0220816831",
            isPrimaryAmbulance = false
        )
    )
}
