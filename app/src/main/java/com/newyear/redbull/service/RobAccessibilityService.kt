package com.newyear.redbull.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.newyear.redbull.RedBullApplication
import com.newyear.redbull.data.RedPacketState
import com.newyear.redbull.data.RedPacketViewDetail
import com.newyear.redbull.data.UserPreferencesRepository
import com.newyear.redbull.model.BasicFunctionalityState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

    private lateinit var repository: UserPreferencesRepository

    private var redPacketList = ArrayDeque<RedPacketNode>()

    private var redPacketState = RedPacketState.FETCHING

    private var currentRedPacket: RedPacketNode = RedPacketNode(node = AccessibilityNodeInfo())

    private var basicFunctionalityInfo = BasicFunctionalityState()

    private val visitedRedPacket = mutableSetOf<Int>()

    private val focusedEvent = intArrayOf(
        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
    )

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "onServiceConnected: ")
        val application = application as RedBullApplication
        repository = application.userPreferencesRepository
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        CoroutineScope(Dispatchers.Default).launch {
            updateParam()
        }
        when {
            event?.packageName == "com.tencent.mm" && event.eventType in focusedEvent -> {
                handleEvent(event.source)
            }
        }
    }

    private suspend fun updateParam() {
        repository.basicFunctionalityPreferences.collectLatest {
            basicFunctionalityInfo = it
        }
    }

    private fun handleEvent(rootNode: AccessibilityNodeInfo?) {
        if (rootNode == null) return
        when (redPacketState) {
            RedPacketState.FETCHING -> findAllRedPacketInWindow(rootNode)
            RedPacketState.FETCHED -> {
                if (redPacketList.isEmpty()) {
                    redPacketState = RedPacketState.FETCHING
                    visitedRedPacket.clear()
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
                    }
                    return
                }
                Thread.sleep(basicFunctionalityInfo.delayCloseSeconds.toLong())
                redPacketState = RedPacketState.FETCHED
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
        }
    }

    private fun findAllRedPacketInWindow(rootNode: AccessibilityNodeInfo) {
        if (!basicFunctionalityInfo.autoOpenRedPacket) return
        getOtherRedPackets(rootNode)
        if (basicFunctionalityInfo.openRedPacketMySelf) {
            getMineRedPackets(rootNode)
        }
        if (redPacketList.isNotEmpty()) {
            redPacketState = RedPacketState.FETCHED
        }
    }

    private fun getOtherRedPackets(rootNode: AccessibilityNodeInfo) {
        val otherRedPacketNodeList = rootNode.findAccessibilityNodeInfosByViewId(RedPacketViewDetail.RED_PACKET_VIEW_OTHER.viewId)
        otherRedPacketNodeList?.forEach {
            val statusNode = it.findAccessibilityNodeInfosByViewId(RedPacketViewDetail.RED_PACKET_STATUS_TEXT.viewId)
            val textNode = it.findAccessibilityNodeInfosByViewId(RedPacketViewDetail.RED_PACKET_VIEW_MINE.viewId)

            visitedRedPacket.add(textNode?.first().hashCode())

            if (statusNode.isNullOrEmpty()) {
                var clickableNode = it
                while (clickableNode != null && !clickableNode.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK)) {
                    clickableNode = clickableNode.getParent()
                }
                redPacketList.add(RedPacketNode(node = clickableNode))
            }
        }
    }

    private fun getMineRedPackets(rootNode: AccessibilityNodeInfo) {
        val mineRedPacketNodeList = rootNode.findAccessibilityNodeInfosByViewId(RedPacketViewDetail.RED_PACKET_VIEW_MINE.viewId)
        mineRedPacketNodeList.forEach{
            if (visitedRedPacket.contains(it.hashCode())) {
                return@forEach
            }

            var clickableNode = it
            while (clickableNode != null && !clickableNode.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK)) {
                clickableNode = clickableNode.getParent()
            }

            val statusNode = clickableNode.findAccessibilityNodeInfosByViewId(RedPacketViewDetail.RED_PACKET_STATUS_TEXT.viewId)

            if (statusNode.isNullOrEmpty()) {
                redPacketList.add(RedPacketNode(node = clickableNode))
            }
        }
    }

    private fun openRedPacket(node : AccessibilityNodeInfo): Boolean {
        val findResult = node.findAccessibilityNodeInfosByViewId(RedPacketViewDetail.OPEN_PACKET_BTN.viewId)
        if (findResult.isEmpty()) {
            return false
        }
        Thread.sleep(basicFunctionalityInfo.delaySeconds.toLong())
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

    fun closeService() {
        disableSelf()
        visitedRedPacket.clear()
    }
}