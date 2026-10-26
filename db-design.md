# DB設計書：やること管理アプリ

## 1. 目的と対象

この文書は、やること管理アプリがMySQLに保存するデータの設計書です。
対象テーブルは `todos` の1つだけです。やること1件を1行として保存します。

## 2. テーブル一覧

| テーブル名 | 内容 | 1行が表すもの |
|---|---|---|
| `todos` | やることを保存する | 登録したやること1件 |

## 3. todosテーブルの列設計

| No. | 列名 | 型 | NULLを許可するか | 既定値・自動設定 | 説明 |
|---|---|---|---|---|---|
| 1 | `id` | `BIGINT` | 許可しない | 自動で1ずつ増える | やること1件を見分ける番号。主キー。 |
| 2 | `title` | `VARCHAR(255)` | 許可しない | なし | やることの名前。 |
| 3 | `detail` | `VARCHAR(255)` | 許可する | なし | やることのメモ。 |
| 4 | `category` | `VARCHAR(255)` | 許可しない | なし | ジャンル。 |
| 5 | `priority` | `INT` | 許可しない | `2` | 優先度。1=高、2=中、3=低。 |
| 6 | `due_date` | `DATE` | 許可する | なし | 期限。 |
| 7 | `completed` | `BOOLEAN` | 許可しない | `FALSE`（未完了） | 完了したかどうか。 |
| 8 | `created_at` | `DATETIME` | 許可しない | 登録時にMySQLが自動設定 | 登録日時。 |
| 9 | `updated_at` | `DATETIME` | 許可しない | 登録時と更新時にMySQLが自動設定 | 最終更新日時。 |

## 4. 制約と値の決まり

| 対象 | 決まり | 目的 |
|---|---|---|
| `id` | 主キー、`AUTO_INCREMENT` | 同じ番号のやることを作らず、番号を自動採番します。 |
| `title` | `NOT NULL` | やることを必ず保存します。 |
| `category` | `NOT NULL` と5つのジャンルだけを許可 | ジャンル未選択や、対象外の値を保存しないためです。 |
| `priority` | `NOT NULL`、既定値は2、1〜3だけを許可 | 優先度を高・中・低の3段階に限定します。 |
| `completed` | `NOT NULL`、既定値は`FALSE` | 新規登録時は未完了として保存します。 |
| `created_at` | `NOT NULL`、登録時に自動設定 | 利用者が入力せずに登録日時を記録します。 |
| `updated_at` | `NOT NULL`、登録時・更新時に自動設定 | 利用者が入力せずに最終更新日時を記録します。 |

ジャンルとして保存できる値は、デザイン、マーケティング、プログラミング、資格、就職活動の5つです。

## 5. 要件とカラムの対応

| 要件 | 対応する列 | 対応内容 |
|---|---|---|
| 一覧でやることを表示する | `title` | やることの名前を表示します。 |
| 一覧でジャンルを表示・絞り込む | `category` | 5つのジャンルを保存し、完全一致で絞り込みます。 |
| 一覧で優先度を表示する | `priority` | 1・2・3を画面では高・中・低として表示します。 |
| 一覧で期限を表示・並び替える | `due_date` | 期限を保存し、期限順に並び替えます。 |
| 一覧で状態を表示する | `completed` | 完了・未完了の状態を保存します。 |
| 登録する | `title`、`detail`、`category`、`priority`、`due_date` | 登録画面の5項目を保存します。 |
| 編集する | `title`、`detail`、`category`、`priority`、`due_date`、`completed` | 編集画面の6項目を更新します。 |
| 削除する | `id` | 削除する1件を番号で特定します。 |
| 1件を編集・削除対象として開く | `id` | URLの `{id}` と対応します。 |
| 登録日時を自動記録する | `created_at` | MySQLが登録時に設定します。 |
| 最終更新日時を自動記録する | `updated_at` | MySQLが更新時に設定します。 |
| 登録・編集・削除のログにIDを残す | `id` | ログに対象の番号を記録します。 |

## 6. CREATE TABLE文（DDL）

DDLは、テーブルを作るためのSQLです。次のSQLは設計書として記載するものであり、この作業では実行しません。

```sql
CREATE TABLE todos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    detail VARCHAR(255) NULL,
    category VARCHAR(255) NOT NULL,
    priority INT NOT NULL DEFAULT 2,
    due_date DATE NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT chk_todos_category
        CHECK (category IN ('デザイン', 'マーケティング', 'プログラミング', '資格', '就職活動')),
    CONSTRAINT chk_todos_priority
        CHECK (priority IN (1, 2, 3))
);
```

## 7. DDLと要件の対応

| DDLの記述 | 対応する要件 |
|---|---|
| `id BIGINT NOT NULL AUTO_INCREMENT` と `PRIMARY KEY (id)` | `id` は自動で増える主キーです。 |
| `title VARCHAR(255) NOT NULL` | やることは必須で、255文字までです。 |
| `detail VARCHAR(255) NULL` | メモは任意で、255文字までです。 |
| `category VARCHAR(255) NOT NULL` と `chk_todos_category` | ジャンルは必須で、定められた5つから選びます。 |
| `priority INT NOT NULL DEFAULT 2` と `chk_todos_priority` | 優先度は必須、既定値は中（2）、値は1〜3です。 |
| `due_date DATE NULL` | 期限は任意の日付です。 |
| `completed BOOLEAN NOT NULL DEFAULT FALSE` | 完了状態は必須で、新規登録時は未完了です。 |
| `created_at ... DEFAULT CURRENT_TIMESTAMP` | 登録日時をMySQLが自動設定します。 |
| `updated_at ... ON UPDATE CURRENT_TIMESTAMP` | 最終更新日時をMySQLが自動設定・更新します。 |

## 8. 叩き台にない追加の扱い

要件にある値の決まりをテーブル作成時にも守るため、DDLに `CHECK` 制約を2つ記載しました。

- `chk_todos_category`：ジャンルを5つに限定します。
- `chk_todos_priority`：優先度を1・2・3に限定します。

これらは新しい画面や機能を追加するものではなく、既存要件の「選べる値」をデータベース側でも守るための設計上の記述です。
