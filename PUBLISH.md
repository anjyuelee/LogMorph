# 發佈到 JitPack

## 快速發佈

```bash
./publish-to-jitpack.sh v1.0.0
```

## 手動發佈

```bash
# 1. 提交變更
git add .
git commit -m "Release v1.0.0"
git push

# 2. 建立並推送 tag
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0

# 3. 前往 GitHub 建立 Release
# https://github.com/anjyuelee/LogMorph/releases
```

## 驗證

檢查 JitPack 建置狀態：https://jitpack.io/#anjyuelee/LogMorph

