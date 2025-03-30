 <div style="background-color:#f1f9ff; border-left: 5px solid #4285f4; padding: 10px; margin-bottom: 10px;">  注意！！！<br>
1.每天写代码前，更新本地代码！<br>
2.不要随意merge到dev分支，不要动main分支！</div>

### **一、基础开发流程**

#### 1. 分支管理策略（推荐）
- `main` 分支：稳定版本，仅用于发布。
- `dev` 分支：开发主分支，合并各功能。
- `feature/xxx` 分支：个人开发的功能分支（如 `feature/login`）。

#### 2. 日常开发步骤（以开发登录功能为例）
```bash
# 1. 从dev分支创建自己的功能分支
git checkout dev
git pull origin dev       # 确保本地dev最新
git checkout -b feature/login

# 2. 编写代码（如修改login.html、添加login.js）

# 3. 提交到本地仓库
git add .                # 添加所有修改
git commit -m "feat: 完成登录页面UI开发"

# 4. 推送功能分支到远程
git push origin feature/login
```

---

### **二、协作与代码合并**
#### 1. 发起合并请求（Pull Request）
1. 在GitHub仓库页面，选择 `feature/login` 分支，点击 `New Pull Request`。
2. 选择将 `feature/login` 合并到 `dev` 分支。
3. 组内代码审核后，合并分支。（不确定时不要自行merge，问组长！）

#### 2. 同步最新代码（重要！）
每天开始工作前，更新本地代码：
```bash
git checkout dev
git pull origin dev      # 拉取远程最新dev代码
```

---

### **三、重要注意事项**
1. **频繁提交**：小步提交，避免一次修改过多文件。
2. **分支命名规范**：建议使用 `feature/功能名`、`fix/问题描述`。
3. **.gitignore文件**：忽略临时文件（如 `node_modules/`, `*.log`）。
4. **禁止直接操作main分支**：通过合并请求审核代码。

---

### **四、常用命令速查**
| 场景             | 命令                         |
| ---------------- | ---------------------------- |
| 查看状态         | `git status`                 |
| 查看提交历史     | `git log --oneline`          |
| 撤销未提交的修改 | `git checkout -- 文件名`     |
| 回退到某次提交   | `git reset --hard commit-id` |
| 暂存临时修改     | `git stash`                  |
