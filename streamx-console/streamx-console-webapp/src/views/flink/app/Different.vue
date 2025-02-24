<template>
  <a-drawer
    :mask-closable="false"
    width="calc(100% - 20%)"
    placement="right"
    :closable="true"
    @close="handleCancel"
    :visible="visiable"
    :destroyOnClose="destroy"
    class="drawer-conf">

    <template slot="title">
      <template>
        <a-icon type="deployment-unit"/>
        Original
        version
        <a-button
          type="primary"
          shape="circle"
          size="small">
          {{ original }}
        </a-button>
        VS  Target version
        <a-button
          type="primary"
          shape="circle"
          size="small">
          {{ modified }}
        </a-button>
      </template>
    </template>

    <a-tabs
      type="card"
      @change="handleChangeTab">
      <a-tab-pane v-for="(item,index) in items" :key="index">
        <template slot="tab">
          {{ item.name }}
        </template>
        <div :id="'mergely'.concat(index)"></div>
      </a-tab-pane>
    </a-tabs>

  </a-drawer>
</template>

<script>
import monaco from './Monaco.yaml'

export default {
  name: 'Different',
  data() {
    return {
      visiable: false,
      destroy: true,
      activeTab: 0,
      items: [],
      diffEditor: null,
      original: null,
      modified : null,
      option: {
        theme: 'vs', //vs, hc-black, or vs-dark
        language: 'yaml',
        selectOnLineNumbers: false,
        foldingStrategy: 'indentation', // 代码分小段折叠
        overviewRulerBorder: false, // 不要滚动条边框
        autoClosingBrackets: true,
        tabSize: 2, // tab 缩进长度
        readOnly: false,
        inherit: true,
        scrollBeyondLastLine: false,
        lineNumbersMinChars: 5,
        lineHeight: 24,
        automaticLayout: true,
        cursorBlinking: 'line',
        cursorStyle: 'line',
        cursorWidth: 3,
        renderFinalNewline: true,
        renderLineHighlight: 'all',
        quickSuggestionsDelay: 100,  //代码提示延时
        scrollbar: {
          useShadows: false,
          vertical: 'visible',
          horizontal: 'visible',
          horizontalSliderSize: 5,
          verticalSliderSize: 5,
          horizontalScrollbarSize: 15,
          verticalScrollbarSize: 15
        }
      }
    }
  },

  methods: {

    different(param,original,modified) {
      this.visiable = true
      this.original = original
      this.modified = modified
      this.items = param
      this.activeTab = 0
      this.handleRenderTab(this.activeTab)
    },

    handleRenderTab(index) {
      const x = this.items[index]
      const ele = '#mergely' + index
      this.$nextTick(() => {
        const elem = document.querySelector(ele)
        this.handleHeight(elem)
        const originalModel = monaco.editor.createModel(x.original, x.format)
        const modifiedModel = monaco.editor.createModel(x.modified, x.format)
        if (this.diffEditor != null) {
          try {
            this.diffEditor.dispose()
          }catch (e) {}
        }
        this.diffEditor = monaco.editor.createDiffEditor(elem, this.option)
        this.diffEditor.setModel({
          original: originalModel,
          modified: modifiedModel
        })
      })
    },

    handleChangeTab(key) {
      this.activeTab = key
      this.handleRenderTab(this.activeTab)
    },

    handleCancel() {
      this.activeTab = 0
      this.visiable = false
      if (this.diffEditor != null) {
        try {
          this.diffEditor.dispose()
        }catch (e) {}
      }
    },

    handleHeight(elem, h) {
      const height = document.documentElement.offsetHeight || document.body.offsetHeight
      $(elem).css('height', (height - 140) + 'px')
    }
  }

}

</script>

<style scoped>
.drawer-conf >>> .ant-drawer-body {
  padding: 5px !important;
  padding-bottom: 0px !important;
}

.drawer-bottom-button {
  position: absolute;
  padding-top: 10px;
  padding-right: 50px;
  width: 100%;
  bottom: 10px;
  z-index: 9;
}

.drwaer-button-item {
  margin-right: 20px;
}
</style>
