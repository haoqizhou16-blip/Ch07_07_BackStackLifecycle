# AI 使用说明（AI_USAGE.md）

> 按《微型 Demo 清单·第三批次》总规范记录 AI 使用情况。

## 一、基本信息

| 项 | 内容 |
| --- | --- |
| Demo | 第 21 项 · Demo 21 Fragment 返回栈与视图生命周期 |
| 工程 | Ch07_07_BackStackLifecycle |
| 使用等级 | 三级（AI 辅助生成初稿，人工审查、修改并实际运行验证） |
| 工具名称 | WorkBuddy（AI 编程助手，桌面端） |
| 使用日期 | 2026-11-25 |
| 运行环境 | Windows + Android Studio（AGP 9.5.2 / Gradle 9.6.0 / 自带 JBR） |

## 二、使用目的

1. 搭建可独立编译运行的工程骨架（Gradle 配置、Manifest、主题、目录）。
2. 生成 A/B 两个 Fragment、统一日志基类、共享 ViewModel 的初稿。
3. 编写文档骨架，并把日志整理成任务要求的"两张记录"（生命周期序列表、观察者差异说明）。

## 三、提示词摘要

1. "帮我完成第三批次第 21 项 Demo：Fragment 返回栈与视图生命周期，工程名 Ch07_07_BackStackLifecycle，Java + XML。"
2. "A → B 用 replace + addToBackStack，返回用 popBackStack；所有 Fragment 生命周期用统一 TAG 打日志。"
3. "在 A 里同时用 getViewLifecycleOwner() 和 this 注册同一个 LiveData 的观察者，做错误对照，要能看出重复回调和泄漏。"
4. "把真实运行日志整理成两张记录，写进 README；至少记录一个真实 Bug。"

## 四、采纳与修改内容

| 部分 | AI 初稿 | 人工处理 |
| --- | --- | --- |
| 观察者数量证据 | 用 `LiveData.getObserverCount()` | 编译报"找不到符号"（lifecycle 2.8 的 Kotlin API 对 Java 不可见），改为在 ViewModel 自建注册计数，见 BUG_RECORD 问题 1 |
| 次要按钮 | 复用信息框背景色 #F1F3F4 | 与禁用态混淆，改为浅蓝底蓝字，见 BUG_RECORD 问题 2 |
| 首次 A 是否入栈 | 初稿也入了栈 | 复核发现返回会白屏，改为首次不入栈 |
| `[this]` 观察者回调 | 初稿在回调里直接 `setText` | 改为只打日志并判断 `getView()` 是否为空，避免视图销毁后空指针 |
| 生命周期日志表 | 由 AI 按常识推断顺序 | **全部替换为本工程真实运行输出的日志**（`docs/lifecycle-log.txt`），逐条核对后填表 |
| A 页发数据按钮 | 初稿只有"跳转 B" | 增加"发送一条数据"，方便现场不切页也能对比两个观察者的回调次数 |

## 五、验证方法

1. **编译验证**：`gradlew.bat assembleDebug` → `BUILD SUCCESSFUL`，产出 `app-debug.apk`。
2. **日志采集**：本机没有可用的模拟器镜像与硬件加速驱动，也无法连接真机，因此用 Robolectric 的 Android 运行时把 A→B→返回→发数据的完整流程真跑了一遍，抓取程序真实 `Log.d` 输出，9 个阶段原文见 `docs/lifecycle-log.txt`。
3. **关键结论验证**：
   - 返回栈深度：②=1，④=0，与 `addToBackStack` / `popBackStack` 预期一致；
   - 重复回调：第 ④ 段 `[A][this]` 打印 **2 次**、`[A][viewLifecycleOwner]` 打印 1 次；
   - 泄漏：累计注册次数 2 → 4 → 6 只增不减；
   - 返回键：⑧ 栈空时 `activity.isFinishing() = true`，⑨ 完整走完 `onDestroyView → onDestroy → onDetach`。
4. **界面验证**：`screenshots/result.png` 由真实布局渲染导出（缺系统状态栏），关键页面见 `screenshots/fragment-a.png`、`fragment-b.png`。

## 六、人工贡献说明

- 全部核心逻辑已逐行阅读，能脱稿讲清"A 为什么只到 CREATED 而不是 DESTROYED""为什么 `this` 观察者在视图销毁后不崩但会多回调"；
- 两处实际问题的定位与修复按 BUG_RECORD 的过程操作并验证；
- 生命周期序列表、观察者差异表均以真实日志为依据填写，未使用 AI 推断的顺序。
