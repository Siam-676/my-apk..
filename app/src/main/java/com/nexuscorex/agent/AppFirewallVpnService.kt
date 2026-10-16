package com.nexuscorex.agent

import android.content.Intent
import android.net.VpnService
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import java.util.concurrent.ConcurrentHashMap

class AppFirewallVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        const val ACTION_APPLY_RULES = "com.nexuscorex.agent.ACTION_APPLY_RULES"
        const val EXTRA_PACKAGE = "extra_package"
        const val EXTRA_BLOCK = "extra_block"
        const val EXTRA_DURATION_SEC = "extra_duration_sec"
        private val blockedPackages = ConcurrentHashMap<String, Runnable>()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_APPLY_RULES) {
            val pkg = intent.getStringExtra(EXTRA_PACKAGE) ?: return START_STICKY
            val shouldBlock = intent.getBooleanExtra(EXTRA_BLOCK, false)
            val durationSec = intent.getLongExtra(EXTRA_DURATION_SEC, 0L)
            updateAppRule(pkg, shouldBlock, durationSec)
        }
        return START_STICKY
    }

    private fun updateAppRule(pkg: String, block: Boolean, durationSec: Long) {
        blockedPackages.remove(pkg)?.let { mainHandler.removeCallbacks(it) }
        if (block) {
            val expiryTask = Runnable {
                blockedPackages.remove(pkg)
                rebuildVpnSinkhole()
            }
            blockedPackages[pkg] = expiryTask
            if (durationSec > 0) {
                mainHandler.postDelayed(expiryTask, durationSec * 1000L)
            }
        }
        rebuildVpnSinkhole()
    }

    private fun rebuildVpnSinkhole() {
        try {
            vpnInterface?.close()
            vpnInterface = null
            if (blockedPackages.isEmpty()) {
                stopSelf()
                return
            }
            val builder = Builder()
                .setSession("NexusCoreX-AppFirewall")
                .addAddress("10.215.173.1", 32)
                .addRoute("0.0.0.0", 0)
                .setBlocking(false)

            for (pkgName in blockedPackages.keys()) {
                try { builder.addAllowedApplication(pkgName) } catch (_: Exception) {}
            }
            vpnInterface = builder.establish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        vpnInterface?.close()
        super.onDestroy()
    }
}
