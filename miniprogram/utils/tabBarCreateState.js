/**
 * TabBar Create 跨页接力全局状态（多实例共享，仅目标 Tab 页消费）
 */

let showCreate = false
let handoffPending = false
let handoffSnapshot = false
let handoffTargetTab = ''
let handoffId = 0

function getShowCreate() {
	return showCreate
}

function setShowCreate(value) {
	showCreate = !!value
}

function getHandoffSnapshot() {
	return handoffSnapshot
}

function getHandoffTargetTab() {
	return handoffTargetTab
}

function getHandoffId() {
	return handoffId
}

function isHandoffPending() {
	return handoffPending
}

function peekHandoff() {
	if (!handoffPending) {
		return null
	}
	return {
		snapshot: handoffSnapshot,
		targetTab: handoffTargetTab,
		id: handoffId
	}
}

function beginHandoff(snapshot, targetTab) {
	handoffId += 1
	handoffSnapshot = !!snapshot
	handoffTargetTab = targetTab
	handoffPending = true
	setShowCreate(handoffSnapshot)
}

function consumeHandoff() {
	if (!handoffPending) {
		return null
	}
	handoffPending = false
	return {
		snapshot: handoffSnapshot,
		targetTab: handoffTargetTab,
		id: handoffId
	}
}

function resetHandoff(finalShowCreate) {
	handoffPending = false
	handoffTargetTab = ''
	handoffSnapshot = false
	if (finalShowCreate !== undefined) {
		setShowCreate(finalShowCreate)
	}
}

module.exports = {
	getShowCreate,
	setShowCreate,
	getHandoffSnapshot,
	getHandoffTargetTab,
	getHandoffId,
	isHandoffPending,
	peekHandoff,
	beginHandoff,
	consumeHandoff,
	resetHandoff
}
