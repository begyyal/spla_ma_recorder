# 概要

スプラトゥーン2の戦歴を記録して勝率をグラフ化する。  
MAは語弊があるかもしれないが、各時点での過去50戦の勝率を長期間に亘って可視化する。  

連敗マッチ後の連勝バイアスを利用してXを目指すことが目的のツール。  
amazonレビューやら自分の経験からしてもこの傾向はどうやらガチマッチの種別を跨ぐように見える。  
おそらく外為のレンジ相場の如く分かりやすい波がグラフに表れるはずなので、  
これを使うことで目先で勝率が上昇するか下降するかの検討がつくことも期待している。

## 実行環境

- Java15以上の実行環境を用意してください。  
    - https://www.oracle.com/java/technologies/downloads/  

## 起動方法

イカリング2の認証で使用される`iksm_session`の値があることが前提となっています。  
検索するといろいろ出てくるのでここでは省略します。  

### 設定ファイルの記述

1. `resources/common.properties.sample`の末尾の`.sample`を消してください。
2. `iksm=`に続く形でiksm_session値を記入してください。
3. その他、設定項目は以下の通りです。任意で設定できます。  

|Key|Value|
|:---|:---|
|iksm|iksm_session値|
|tablePath|保存するデータのファイルパス(ファイル名含め自由)|
|term|グラフ横軸の表示期間の設定値。カンマ区切りによる複数の値を設定する形式で、画面上部のコンボボックスの内容値となる。|
|pollingIntervalSec|イカリングAPIへのポーリング間隔(秒)|
|windowHeight|起動時に表示される画面の高さ(ピクセル)|
|windowWidth|起動時に表示される画面の幅(ピクセル)|

### 実行

リポジトリのルートにて以下のコマンドを実行してください。

1. `gradlew compileJava`
2. `gradlew run`

## 機能

画面上の表記に紐づけて記します。

- Type
  - ゲーム種別です。ガチマッチやレギュラーマッチが該当します。
  - リーグマッチ等は概要記載の主旨に沿わない(であろう)ため対応していません。
  - `TOTAL`は全種を総計した内容になります。
- Rule
  - ゲームのルールです。ガチマッチであればガチホコやガチヤグラ等が該当します。
  - ゲーム種別内の全ルールを総計したものを形式上 `[種別]_ALL` としています。
  - レギュラーマッチのルールはナワバリのみのため、`REGULAR_ALL`はありません。
- Term
  - 上述「設定ファイルの記述」内のtermを参照してください。
- Pararrel Prediction
  - 「先の試合の結果が何であれば現在の勝率が維持されるか？」といった内容になります。
    - 例えば現在の勝率が50％でこの予測が向こう10試合での勝ち越しを示していた場合、  
      向こう10試合で勝ち越しても勝率は変わりません。 
  - ラベル内の数字が向こう何試合先かを示しています。例えば左端は1なので次の試合を指します。
  - 赤が勝ち、青が負けを示してします。
- Current Rate
  - 現在の勝率です。

## 補足

- 起動中に戦績を取得している都合上、**停止中に51戦以上試合をすると勝率の断面の記録が漏れます。**  
- 逆に50戦以内であれば勝率を補完しますが、漏れなく記録したい場合は長時間遊ぶ際にアプリを起動しておくことをおススメします。
