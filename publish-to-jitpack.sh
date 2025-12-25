#!/bin/bash

echo "🚀 發佈到 JitPack"
echo ""

# 檢查版本號參數
if [ -z "$1" ]; then
    echo "用法: ./publish-to-jitpack.sh <版本號>"
    echo "範例: ./publish-to-jitpack.sh v1.0.0"
    exit 1
fi

TAG_VERSION=$1

# 提交變更
if [[ -n $(git status -s) ]]; then
    echo "📝 提交變更..."
    git add .
    git commit -m "Release $TAG_VERSION"
fi

# 推送到 GitHub
echo "🌐 推送到 GitHub..."
git push origin $(git branch --show-current)

# 建立並推送 tag
echo "🏷️  建立 tag: $TAG_VERSION"
git tag -a "$TAG_VERSION" -m "Release $TAG_VERSION"
git push origin "$TAG_VERSION"

echo ""
echo "✅ 完成！"
echo ""
echo "下一步:"
echo "1. 前往 GitHub 建立 Release:"
echo "   https://github.com/anjyuelee/LogMorph/releases/new?tag=$TAG_VERSION"
echo ""
echo "2. 或直接到 JitPack 檢查建置狀態:"
echo "   https://jitpack.io/#anjyuelee/LogMorph"

