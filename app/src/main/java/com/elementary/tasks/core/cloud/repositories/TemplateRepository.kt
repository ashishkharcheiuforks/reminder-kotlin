package com.elementary.tasks.core.cloud.repositories

import com.elementary.tasks.core.data.models.SmsTemplate

class TemplateRepository : DatabaseRepository<SmsTemplate>() {
    override suspend fun get(id: String): SmsTemplate? {
        return appDb.smsTemplatesDao().getByKey(id)
    }

    override suspend fun insert(t: SmsTemplate) {
        appDb.smsTemplatesDao().insert(t)
    }

    override suspend fun all(): List<SmsTemplate> {
        return appDb.smsTemplatesDao().all()
    }

    override suspend fun delete(t: SmsTemplate) {
        appDb.smsTemplatesDao().delete(t)
    }
}