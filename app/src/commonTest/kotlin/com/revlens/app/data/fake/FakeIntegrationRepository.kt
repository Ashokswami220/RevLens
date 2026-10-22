package com.revlens.app.data.fake

import com.revlens.app.domain.model.ConnectionStatus
import com.revlens.app.domain.model.IntegrationConnection
import com.revlens.app.domain.model.IntegrationProvider
import com.revlens.app.domain.repository.IntegrationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeIntegrationRepository : IntegrationRepository {

    private val connectionsByWs =
        mutableMapOf<String, MutableStateFlow<List<IntegrationConnection>>>()

    private fun getFlow(workspaceId: String): MutableStateFlow<List<IntegrationConnection>> {
        return connectionsByWs.getOrPut(workspaceId) {
            MutableStateFlow(
                listOf(
                    IntegrationConnection(
                        id = "conn_stripe",
                        provider = IntegrationProvider.STRIPE,
                        status = ConnectionStatus.CONNECTED,
                        accountName = "acct_1N9qZ2RevLens",
                        lastSyncAt = "10 minutes ago",
                        grantedScopes = IntegrationProvider.STRIPE.defaultScopes
                    ),
                    IntegrationConnection(
                        id = "conn_paddle",
                        provider = IntegrationProvider.PADDLE,
                        status = ConnectionStatus.DISCONNECTED
                    ),
                    IntegrationConnection(
                        id = "conn_sheets",
                        provider = IntegrationProvider.GOOGLE_SHEETS,
                        status = ConnectionStatus.DISCONNECTED
                    ),
                    IntegrationConnection(
                        id = "conn_slack",
                        provider = IntegrationProvider.SLACK,
                        status = ConnectionStatus.CONNECTED,
                        accountName = "#rev-alerts (HQ)",
                        lastSyncAt = "Today at 09:30 AM",
                        grantedScopes = IntegrationProvider.SLACK.defaultScopes
                    )
                )
            )
        }
    }

    override fun getConnections(workspaceId: String): Flow<List<IntegrationConnection>> =
        getFlow(workspaceId).asStateFlow()

    override suspend fun connectIntegration(
        workspaceId: String,
        provider: IntegrationProvider
    ): Result<IntegrationConnection> {
        val flow = getFlow(workspaceId)
        val accountName = when (provider) {
            IntegrationProvider.STRIPE -> "acct_revlens_live"
            IntegrationProvider.PADDLE -> "seller_882910_revlens"
            IntegrationProvider.GOOGLE_SHEETS -> "team-models@gmail.com"
            IntegrationProvider.SLACK -> "#growth-metrics"
        }

        val updatedConn = IntegrationConnection(
            id = "conn_${provider.id}",
            provider = provider,
            status = ConnectionStatus.CONNECTED,
            accountName = accountName,
            lastSyncAt = "Just now",
            grantedScopes = provider.defaultScopes
        )

        flow.update { list ->
            list.map { if (it.provider == provider) updatedConn else it }
        }

        return Result.success(updatedConn)
    }

    override suspend fun disconnectIntegration(
        workspaceId: String,
        provider: IntegrationProvider
    ): Result<Unit> {
        val flow = getFlow(workspaceId)
        val disconnectedConn = IntegrationConnection(
            id = "conn_${provider.id}",
            provider = provider,
            status = ConnectionStatus.DISCONNECTED,
            accountName = null,
            lastSyncAt = null,
            grantedScopes = emptyList()
        )

        flow.update { list ->
            list.map { if (it.provider == provider) disconnectedConn else it }
        }

        return Result.success(Unit)
    }

    override suspend fun syncIntegration(
        workspaceId: String,
        provider: IntegrationProvider
    ): Result<IntegrationConnection> {
        val flow = getFlow(workspaceId)
        val existing = flow.value.find { it.provider == provider }
            ?: return Result.failure(NoSuchElementException("Integration not configured"))

        val synced = existing.copy(
            status = ConnectionStatus.CONNECTED,
            lastSyncAt = "Just now"
        )

        flow.update { list ->
            list.map { if (it.provider == provider) synced else it }
        }

        return Result.success(synced)
    }
}
