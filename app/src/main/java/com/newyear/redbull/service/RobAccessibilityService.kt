package com.newyear.redbull.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.newyear.redbull.data.RedPacketState
import com.newyear.redbull.data.RedPacketViewDetail
import kotlinx.coroutines.delay
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
    }

    private var redPacketList = ArrayDeque<RedPacketNode>()

    private var redPacketState = RedPacketState.FETCHING

    private var currentRedPacket: RedPacketNode = RedPacketNode(node = AccessibilityNodeInfo())

    private val focusedEvent = intArrayOf(
        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "onServiceConnected: ")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
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
                            delay(1500)
                        }
                        performGlobalAction(GLOBAL_ACTION_BACK)
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
            delay(200)
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
//        performGlobalAction(GLOBAL_ACTION_BACK)
        whenPerformBack()
    }

    override fun onInterrupt() {}
}