package com.tteumsae.app.platform

import org.junit.Assert.assertEquals
import org.junit.Test

class ExternalSettingsTest {
    @Test
    fun `문의 이메일은 운영 계정 주소를 사용한다`() {
        assertEquals("godburgundy@gmail.com", CONTACT_EMAIL)
    }
}
