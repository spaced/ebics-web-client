(self["webpackChunkebics_client_qs"]=self["webpackChunkebics_client_qs"]||[]).push([[936],{3936:(e,t,a)=>{"use strict";a.r(t),a.d(t,{default:()=>f});var n=a(3673),l=a(2323);const o={class:"q-pa-md"},s={key:0},i={key:1},r={class:"q-pa-md",style:{"max-width":"400px"}};function d(e,t,a,d,m,u){const p=(0,n.up)("q-input"),c=(0,n.up)("q-btn"),h=(0,n.up)("q-form"),b=(0,n.up)("q-page");return(0,n.wg)(),(0,n.j4)(b,{class:" justify-evenly"},{default:(0,n.w5)((()=>[(0,n.Wm)("div",o,[void 0!==e.id?((0,n.wg)(),(0,n.j4)("h5",s,"Edit existing bank "+(0,l.zw)(e.id),1)):((0,n.wg)(),(0,n.j4)("h5",i,"Add new bank")),(0,n.Wm)("div",r,[(0,n.Wm)(h,{onSubmit:t[4]||(t[4]=t=>e.onSubmit(e.$props.id)),onReset:e.onCancel,class:"q-gutter-md"},{default:(0,n.w5)((()=>[(0,n.Wm)(p,{filled:"",modelValue:e.bank.name,"onUpdate:modelValue":t[1]||(t[1]=t=>e.bank.name=t),label:"Bank name *",hint:"User defined name for this bank","lazy-rules":"",rules:[e=>e&&e.length>1||"Bank name must be at least 2 characters"]},null,8,["modelValue","rules"]),(0,n.Wm)(p,{filled:"",modelValue:e.bank.bankURL,"onUpdate:modelValue":t[2]||(t[2]=t=>e.bank.bankURL=t),label:"EBICS URL",hint:"EBICS bank URL, including https://","lazy-rules":"",rules:[t=>t&&t.length>1&&e.validateUrl(t)||"Please enter valid URL including http(s)://"]},null,8,["modelValue","rules"]),(0,n.Wm)(p,{filled:"",modelValue:e.bank.hostId,"onUpdate:modelValue":t[3]||(t[3]=t=>e.bank.hostId=t),label:"EBICS HOSTID",hint:"EBICS HOST ID, example EBXUBSCH","lazy-rules":"",rules:[e=>e&&e.length>0||"Please enter valid EBICS HOST ID, at least 1 character"]},null,8,["modelValue","rules"]),(0,n.Wm)("div",null,[void 0===e.id?((0,n.wg)(),(0,n.j4)(c,{key:0,label:"Add",type:"submit",color:"primary"})):((0,n.wg)(),(0,n.j4)(c,{key:1,label:"Update",type:"submit",color:"primary"})),(0,n.Wm)(c,{label:"Cancel",type:"reset",color:"primary",flat:"",class:"q-ml-sm",icon:"undo"})])])),_:1},8,["onReset"])])])])),_:1})}var m=a(1768);const u=(0,n.aZ)({name:"Bank",components:{},props:{id:{type:Number,required:!1,default:void 0}},data(){return{bank:{bankURL:"",name:"",hostId:""}}},methods:{validateUrl(e){const t=/^(http(s)?:\/\/.)(www\.)?[-a-zA-Z0-9@:%._\+~#=]{0,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)$/;return t.test(e)},loadData(e){m.api.get(`/banks/${e}`).then((e=>{this.bank=e.data})).catch((e=>{this.$q.notify({color:"negative",position:"bottom-right",message:`Loading failed: ${e.message}`,icon:"report_problem"})}))},onSubmit(e){void 0===e?m.api.post("/banks",this.bank).then((()=>{this.$q.notify({color:"green-4",textColor:"white",icon:"cloud_done",message:"Create done"}),this.$router.go(-1)})).catch((e=>{this.$q.notify({color:"negative",position:"bottom-right",message:`Creating failed: ${e.message}`,icon:"report_problem"})})):m.api.put(`/banks/${e}`,this.bank).then((()=>{this.$q.notify({color:"green-4",textColor:"white",icon:"cloud_done",message:"Update done"}),this.$router.go(-1)})).catch((e=>{this.$q.notify({color:"negative",position:"bottom-right",message:`Update failed: ${e.message}`,icon:"report_problem"})}))},onCancel(){this.$router.go(-1)}},mounted(){void 0!==this.$props.id&&this.loadData(this.$props.id)}});var p=a(4379),c=a(5269),h=a(4689),b=a(8240),g=a(7518),k=a.n(g);u.render=d;const f=u;k()(u,"components",{QPage:p.Z,QForm:c.Z,QInput:h.Z,QBtn:b.Z})}}]);