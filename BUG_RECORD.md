# BUG_RECORD.md

> Demo 21　Fragment 返回栈与视图生命周期（工程 `Ch07_07_BackStackLifecycle`）
> 记录开发过程中**实际遇到**的问题：现象 → 原因 → 解决过程 → 对应 Git 提交。

---

## 问题 1：`LiveData.getObserverCount()` 编译不过

**现象**

想用 `LiveData` 自带的 `getObserverCount()` 打印观察者数量作为泄漏证据，编译直接失败：

```
FragmentA.java:53: 错误: 找不到符号
        + viewModel.getMessage().getObserverCount());
                                  ^
  符号:   方法 getObserverCount()
  位置: 类 LiveData<String>

SharedViewModel.java:27: 错误: 找不到符号
        + message.getObserverCount());
                 ^
  符号:   方法 getObserverCount()
  位置: 类型为MutableLiveData<String>的变量 message
3 个错误
```

工程能编译的部分（Fragment 事务、LiveData 观察）都没问题，只有这三处报错。

**原因**

`lifecycle 2.8.x` 的 `LiveData` 已经改成 Kotlin 实现，`getObserverCount()` 不再是 Java 侧可见的公开 API（Kotlin 里它是给测试用的内部方法），所以 Java 代码调用时符号根本不存在，不是权限问题也不是依赖没加。

**解决过程**

1. 先确认依赖没漏：`lifecycle-livedata:2.8.7` 已在 `app/build.gradle` 里，`observe()`、`setValue()` 都正常，排除依赖问题；
2. 改用"自己记账"的方案：在 `SharedViewModel` 里维护 `observeCount`，每次 `observe()` 之前调用 `onObserveRegistered(who)` 打印并自增；
3. 重新编译通过，并把日志语义写清楚：这是"**历史累计注册次数**"，只增不减，和"实际回调次数"的差值就是没被释放的观察者。

```java
public void onObserveRegistered(String who) {
    observeCount++;
    Log.d(TAG, "[ViewModel] 注册观察者 " + who + "，累计注册次数 = " + observeCount);
}
```

改完后的实测效果（见 `docs/lifecycle-log.txt`）：累计注册次数 `1, 2 → 3, 4 → 5, 6`，而 `[viewLifecycleOwner]` 每次数据只回调 1 次、`[this]` 回调 2 次——证据反而比原方案更直观。

**对应提交**：`8ac076d feat: 完成Fragment返回栈与两组观察者对照`

---

## 问题 2：次要按钮和"禁用状态"长得一样

**现象**

效果图里 A 页第二个按钮「发送一条数据」是浅灰底 + 深色字，和 Material 的禁用态几乎无法区分，第一眼看过去像是不能点；B 页的「返回 Fragment A」同样问题。

**原因**

次要按钮直接复用了卡片信息框的背景色 `@color/info_background`（#F1F3F4），而页面背景本身也是 #F3F4F6，两者明度太接近，既没有按钮的"可点击感"，又正好撞上了禁用态的视觉语言。

**解决过程**

新增一个专属的次要色 `secondary_button_bg`（#E8F0FE，浅蓝），文字改成品牌蓝 `@color/brand_blue`，形成"浅蓝底 + 蓝字"的次级按钮样式，既区别于主按钮（实心蓝/绿），也不会被误认为禁用。

**对应提交**：`8ac076d feat: 完成Fragment返回栈与两组观察者对照`

---

## 三、设计复核（不是 Bug，记录备查）

**首次添加 Fragment A 时不能加 `addToBackStack`。**

一开始想"既然是返回栈 Demo，第一次也入栈更完整"，实测发现：如果首次 A 也入栈，在 A 上按返回键会先把 A 弹出，容器变成空的——页面白屏，而不是退出 Activity。所以首次添加保持不入栈：

```java
// 首次：不入栈，A 上按返回直接结束 Activity
.replace(R.id.fragmentContainer, new FragmentA(), "A").commit();

// A → B：入栈，返回键由 FragmentManager 拦截回退
.replace(R.id.fragmentContainer, new FragmentB(), "B").addToBackStack("B").commit();
```

日志第 ⑧ 段验证了这一点：返回栈深度 = 0 时按返回，`activity.isFinishing() = true`。
