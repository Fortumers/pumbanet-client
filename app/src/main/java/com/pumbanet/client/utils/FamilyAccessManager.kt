package com.pumbanet.client.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Семейный доступ (Feature 20)
 * Профили для членов семьи, родительский контроль, лимиты
 */
class FamilyAccessManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Добавить члена семьи
     */
    fun addFamilyMember(name: String, email: String, role: Role): FamilyMember {
        val member = FamilyMember(
            id = generateId(),
            name = name,
            email = email,
            role = role,
            trafficLimit = 0,
            trafficUsed = 0,
            isActive = true
        )
        saveFamilyMember(member)
        return member
    }

    /**
     * Получить всех членов семьи
     */
    fun getFamilyMembers(): List<FamilyMember> {
        val json = prefs.getString(KEY_FAMILY_MEMBERS, null) ?: return emptyList()
        val array = org.json.JSONArray(json)
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            FamilyMember(
                id = obj.optString("id"),
                name = obj.optString("name"),
                email = obj.optString("email"),
                role = Role.valueOf(obj.optString("role", "CHILD")),
                trafficLimit = obj.optLong("traffic_limit"),
                trafficUsed = obj.optLong("traffic_used"),
                isActive = obj.optBoolean("is_active", true)
            )
        }
    }

    private fun saveFamilyMember(member: FamilyMember) {
        val members = getFamilyMembers().toMutableList()
        val existingIndex = members.indexOfFirst { it.id == member.id }
        
        if (existingIndex >= 0) {
            members[existingIndex] = member
        } else {
            members.add(member)
        }

        val array = org.json.JSONArray()
        members.forEach { m ->
            array.put(
                org.json.JSONObject().apply {
                    put("id", m.id)
                    put("name", m.name)
                    put("email", m.email)
                    put("role", m.role.name)
                    put("traffic_limit", m.trafficLimit)
                    put("traffic_used", m.trafficUsed)
                    put("is_active", m.isActive)
                }
            )
        }
        prefs.edit().putString(KEY_FAMILY_MEMBERS, array.toString()).apply()
    }

    /**
     * Установить лимит трафика
     */
    fun setTrafficLimit(memberId: String, limitBytes: Long) {
        val members = getFamilyMembers().toMutableList()
        val index = members.indexOfFirst { it.id == memberId }
        if (index >= 0) {
            members[index] = members[index].copy(trafficLimit = limitBytes)
            saveFamilyMember(members[index])
        }
    }

    /**
     * Обновить использованный трафик
     */
    fun updateTrafficUsed(memberId: String, usedBytes: Long) {
        val members = getFamilyMembers().toMutableList()
        val index = members.indexOfFirst { it.id == memberId }
        if (index >= 0) {
            members[index] = members[index].copy(trafficUsed = usedBytes)
            saveFamilyMember(members[index])
        }
    }

    /**
     * Проверка превышения лимита
     */
    fun isLimitExceeded(memberId: String): Boolean {
        val member = getFamilyMembers().find { it.id == memberId }
        return member?.let { it.trafficLimit > 0 && it.trafficUsed >= it.trafficLimit } ?: false
    }

    private fun generateId(): String {
        return "member_" + System.currentTimeMillis()
    }

    companion object {
        private const val PREFS_NAME = "pumbanet_family"
        private const val KEY_FAMILY_MEMBERS = "family_members"
    }

    data class FamilyMember(
        val id: String,
        val name: String,
        val email: String,
        val role: Role,
        val trafficLimit: Long,
        val trafficUsed: Long,
        val isActive: Boolean
    )

    enum class Role {
        PARENT,
        CHILD
    }
}
