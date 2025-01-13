package com.newyear.redbull.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.newyear.redbull.RedBullApplication
import com.newyear.redbull.data.RedPacketState
import com.newyear.redbull.data.RedPacketViewDetail
import com.newyear.redbull.data.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class RedPacketNode (
    val node: AccessibilityNodeInfo,
    var retry: Int = 0
)

class RobAccessibilityService : AccessibilityService() {

    companion object {
        const val SERVICE_NAME = "com.newyear.redbull.service.RobAccessibilityService"
        private const val TAG = "RobAccessibilityService"
        private const val MAX_RETRY = 3
        private var instance: RobAccessibilityService? = null

        fun getInstance(): RobAccessibilityService? {
            return instance
        }
    }

    private var redPacketList = ArrayDeque<RedPacketNode>()

    private var redPacketState = RedPacketState.FETCHING

    private var currentRedPacket: RedPacketNode = RedPacketNode(node = AccessibilityNodeInfo())

    private lateinit var repository: UserPreferencesRepository

    private var delayOpenRedPacket = 200

    private var delayCloseRedPacket = 0

    private val focusedEvent = intArrayOf(
        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
    )

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "onServiceConnected: ")
        val application = application as RedBullApplication
        repository = application.userPreferencesRepository
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        CoroutineScope(Dispatchers.Main).launch {
            repository.delayOpenSeconds.collectLatest {
                delayOpenRedPacket = it
            }
            repository.delayCloseSeconds.collectLatest {
                delayCloseRedPacket = it
            }
        }
        when {
            event?.packageName == "com.tencent.mm" && event.eventType in focusedEvent -> {
                handleEvent(event.source)
            }
        }
    }

    private fun handleEvent(rootNode: AccessibilityNodeInfo?) {
        if (rootNode == null) return
        when (redPacketState) {
            RedPacketState.FETCHING -> findAllRedPacketInWindow(rootNode)
            RedPacketState.FETCHED -> {
                if (redPacketList.isEmpty()) {
                    redPacketState = RedPacketState.FETCHING
                    return
                }
                currentRedPacket = redPacketList.removeFirst()
                currentRedPacket.node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                redPacketState = RedPacketState.OPENING
            }
            RedPacketState.OPENING -> {
                if (!openRedPacket(rootNode)) {
                    retryAgain{
                        redPacketState = RedPacketState.FETCHED
                    }
                    return
                }
                redPacketState = RedPacketState.OPENED
            }
            RedPacketState.OPENED -> {
                val findResult = rootNode.findAccessibilityNodeInfosByViewId(RedPacketViewDetail.RED_PACKET_DETAIL_TEXT.viewId)
                if (findResult.isEmpty()) {
                    retryAgain{
                        redPacketState = RedPacketState.FETCHED
                        runBlocking {
                            delay(delayCloseRedPacket.toLong())
                        }
                    }
                    return
                }
                redPacketState = RedPacketState.FETCHED
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
        }
    }

    private fun findAllRedPacketInWindow(rootNode: AccessibilityNodeInfo) {
        val redPacketNodeList = rootNode.findAccessibilityNodeInfosByViewId(RedPacketViewDetail.RED_PACKET_VIEW.viewId)
        redPacketNodeList?.forEach {
            var isRedPacketValid = true
            repeat(it.childCount) { index ->
                // Get red packet status
                val childNode = it.getChild(index)
                if (childNode == null) {
                    isRedPacketValid = false
                    return@repeat
                }
                val redPacketStatus = childNode.findAccessibilityNodeInfosByViewId(RedPacketViewDetail.RED_PACKET_STATUS_TEXT.viewId) ?: listOf()
                if (redPacketStatus.isNotEmpty()) {
                    isRedPacketValid = false
                }
            }
            var clickableNode = it.getParent()
            while (clickableNode != null && !clickableNode.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK)) {
                clickableNode = clickableNode.getParent()
            }
            if (isRedPacketValid && clickableNode != null) {
                redPacketList.add(RedPacketNode(node = clickableNode))
            }
        }
        if (redPacketList.isNotEmpty()) {
            redPacketState = RedPacketState.FETCHED
        }
    }

    private fun openRedPacket(node : AccessibilityNodeInfo): Boolean {
        runBlocking {
            delay(delayOpenRedPacket.toLong())
        }
        val findResult = node.findAccessibilityNodeInfosByViewId(RedPacketViewDetail.OPEN_PACKET_BTN.viewId)
        if (findResult.isEmpty()) {
            return false
        }
        val openPacketBtn = findResult.first()
        return openPacketBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun retryAgain(
        whenPerformBack: () -> Unit = {}
    ) {
        if (currentRedPacket.retry < MAX_RETRY) {
            currentRedPacket.apply {
                retry++
            }
            return
        }
        whenPerformBack()
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    override fun onInterrupt() {}
}