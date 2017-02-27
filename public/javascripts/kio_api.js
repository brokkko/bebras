var kio_api=function(e){function t(i){if(n[i])return n[i].exports;var o=n[i]={i:i,l:!1,exports:{}};return e[i].call(o.exports,o,o.exports,t),o.l=!0,o.exports}var n={};return t.m=e,t.c=n,t.i=function(e){return e},t.d=function(e,n,i){t.o(e,n)||Object.defineProperty(e,n,{configurable:!1,enumerable:!0,get:i})},t.n=function(e){var n=e&&e.__esModule?function(){return e.default}:function(){return e};return t.d(n,"a",n),n},t.o=function(e,t){return Object.prototype.hasOwnProperty.call(e,t)},t.p="",t(t.s=8)}([function(e,t,n){"use strict";function i(e,t){if(!(e instanceof t))throw new TypeError("Cannot call a class as a function")}function o(e,t){var n=new l("Результат",t.problem.parameters()),i=new l("Рекорд",t.problem.parameters());t.results_info_panel=n,t.record_info_panel=i;var o=document.createElement("div");o.className="kio-base-info-panels-container",o.appendChild(n.domNode),o.appendChild(i.domNode),e.appendChild(o),n.domNode.className+=" kio-base-results-info-panel",i.domNode.className+=" kio-base-record-info-panel";var a=new u("Очистить решение",function(){t.problem.loadSolution(t.emptySolution)}),s=new u("Загрузить рекорд",function(){t.problem.loadSolution(t.best)});n.domNode.appendChild(a.domNode),i.domNode.appendChild(s.domNode),e.className="kio-base-controls-container";var d=document.createElement("div");d.className="kio-base-clear-both",o.appendChild(d);var c=new r.a(t);e.appendChild(c.domNode)}var a=n(7),r=(n.n(a),n(6));t.a=o,n.d(t,"b",function(){return u});var s=function(){function e(e,t){for(var n=0;n<t.length;n++){var i=t[n];i.enumerable=i.enumerable||!1,i.configurable=!0,"value"in i&&(i.writable=!0),Object.defineProperty(e,i.key,i)}}return function(t,n,i){return n&&e(t.prototype,n),i&&e(t,i),t}}(),l=function(){function e(t,n){i(this,e),this.title=t,this.params=n,this.domNode=document.createElement("div"),this.inject_inside(this.domNode)}return s(e,[{key:"setParams",value:function(e){var t=0,n=!0,i=!1,o=void 0;try{for(var a,r=this.params[Symbol.iterator]();!(n=(a=r.next()).done);n=!0){var s=a.value;if(s.title){var l=this.value_elements[t],u=e[s.name];l.innerText=this.paramViewFunction(s)(u),t++}}}catch(e){i=!0,o=e}finally{try{!n&&r.return&&r.return()}finally{if(i)throw o}}}},{key:"paramViewFunction",value:function(e){return e.view?"function"==typeof e.view?e.view:function(t){return t+e.view}:function(e){return e}}},{key:"inject_inside",value:function(e){e.className="kio-base-info-panel",this.param_name_2_param={},this.param_name_2_value_element={};var t=document.createElement("table"),n=document.createElement("thead"),i=document.createElement("tbody");n.className="kio-base-info-panel-head",t.appendChild(n),t.appendChild(i);var o=document.createElement("tr"),a=document.createElement("td");a.colspan=2,n.appendChild(o),o.appendChild(a),a.innerText=this.title,this.value_elements=[];var r=!0,s=!1,l=void 0;try{for(var u,d=this.params[Symbol.iterator]();!(r=(u=d.next()).done);r=!0){var c=u.value;if(c.title){var m=document.createElement("tr"),h=document.createElement("td"),p=document.createElement("td");m.appendChild(h),m.appendChild(p),h.className="kio-base-info-panel-param-name",p.className="kio-base-info-panel-param-value",h.innerText=c.title,i.appendChild(m),this.value_elements.push(p)}}}catch(e){s=!0,l=e}finally{try{!r&&d.return&&d.return()}finally{if(s)throw l}}e.appendChild(t)}},{key:"as_string",value:function(e){if(!e)return"-";var t=[],n=!0,i=!1,o=void 0;try{for(var a,r=this.params[Symbol.iterator]();!(n=(a=r.next()).done);n=!0){var s=a.value;if(s.title){var l=e[s.name];t.push(this.paramViewFunction(s)(l))}}}catch(e){i=!0,o=e}finally{try{!n&&r.return&&r.return()}finally{if(i)throw o}}return t.join(", ")}}]),e}(),u=function(){function e(t,n){i(this,e),this.domNode=document.createElement("button"),this.domNode.className="kio-base-control-button",this.title=t,$(this.domNode).click(n)}return s(e,[{key:"title",set:function(e){this._title=e,this.domNode.innerText=e},get:function(){return this._title}}]),e}()},,,,,,function(e,t,n){"use strict";function i(e,t){if(!(e instanceof t))throw new TypeError("Cannot call a class as a function")}var o=n(0);n.d(t,"a",function(){return r});var a=function(){function e(e,t){for(var n=0;n<t.length;n++){var i=t[n];i.enumerable=i.enumerable||!1,i.configurable=!0,"value"in i&&(i.writable=!0),Object.defineProperty(e,i.key,i)}}return function(t,n,i){return n&&e(t.prototype,n),i&&e(t,i),t}}(),r=function(){function e(t){i(this,e),this.kio_api=t,this.init_interface();var n=dces2contest.get_all_problem_data_keys(t.pid),o="save-",a=!0,r=!1,l=void 0;try{for(var u,d=n[Symbol.iterator]();!(a=(u=d.next()).done);a=!0){var c=u.value;if(c.substr(0,o.length)==o){var m=c.substr(o.length),h=s.load(this,m);null!==h&&this.solutions_node.appendChild(h.domNode)}}}catch(e){r=!0,l=e}finally{try{!a&&d.return&&d.return()}finally{if(r)throw l}}}return a(e,[{key:"init_interface",value:function(){var e=this;this.domNode=document.createElement("div"),this.domNode.className="kio-base-solutions-container";var t=document.createElement("div");t.className="title",t.innerText="Сохраненные решения",this.domNode.appendChild(t),this.new_solution_node=document.createElement("div"),this.new_solution_node.className="kio-base-new-solution";var n=document.createElement("span");n.innerText="Назовите решение:",this.new_name=document.createElement("input"),this.new_solution_node.appendChild(n),this.new_solution_node.appendChild(this.new_name),this.new_solution_node.appendChild(new o.b("Сохранить",function(t){var n=e.new_name.value;if(n){var i=s.create(e,n,e.kio_api.problem.solution(),e.kio_api.last_submitted_result);0==e.solutions_node.childNodes.length?e.solutions_node.append(i.domNode):e.solutions_node.insertBefore(i.domNode,e.solutions_node.childNodes[0]),e.new_name.value=""}}).domNode),this.message_node=document.createElement("span"),this.new_solution_node.appendChild(this.message_node),this.domNode.appendChild(this.new_solution_node),this.solutions_node=document.createElement("tbody");var i=document.createElement("table");i.appendChild(this.solutions_node),this.domNode.appendChild(i)}}]),e}(),s=function(){function e(t,n,o,a,r){i(this,e),this.stored_solutions=t,this.kio_api=t.kio_api,this.id=n,this.data_key="save-"+n,this.name=o,this.solution=a,this.result=r,this.init_interface()}return a(e,null,[{key:"load",value:function(t,n){var i="save-"+n,o=dces2contest.get_problem_data(t.kio_api.pid,i);if(""===o)return null;var a=JSON.parse(o),r=a.name,s=a.solution,l=a.result;return new e(t,n,r,s,l)}},{key:"create",value:function(t,n,i,o){var a=e.makeid(10),r=new e(t,a,n,i,o);return r.save(),r}},{key:"makeid",value:function(){for(var e="",t="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789",n=0;n<10;n++)e+=t.charAt(Math.floor(Math.random()*t.length));return e}}]),a(e,[{key:"remove",value:function(){dces2contest.save_problem_data(this.kio_api.pid,this.data_key,"")}},{key:"save",value:function(){dces2contest.save_problem_data(this.kio_api.pid,"save-"+this.id,JSON.stringify({name:this.name,solution:this.solution,result:this.result}))}},{key:"init_interface",value:function(){var e=this;this.domNode=document.createElement("tr"),this.domNode.className="kio-base-stored-solution",this.nameNode=document.createElement("span"),this.nameNode.innerText=this.name,this.loadButton=new o.b("Загрузить",function(t){e.kio_api.problem.loadSolution(e.solution)}).domNode,this.removeButton=new o.b("Ctrl+Удалить",function(t){t.ctrlKey&&(e.domNode.parentNode.removeChild(e.domNode),e.remove())}).domNode;var t=document.createElement("td"),n=document.createElement("td"),i=document.createElement("td");t.className="first",i.className="second",t.appendChild(this.nameNode),n.appendChild(this.loadButton),n.appendChild(this.removeButton),this.domNode.appendChild(t),this.domNode.appendChild(i),this.domNode.appendChild(n),i.innerText=this.kio_api.results_info_panel.as_string(this.result)}}]),e}()},function(e,t){},function(e,t,n){"use strict";function i(e,t){if(!(e instanceof t))throw new TypeError("Cannot call a class as a function")}function o(e,t,n,i){function o(e,n){var i=n.loading_queue;t.removeChild(d);var o=new l(u,t,i);o.init_view(t,u),o.saveEmptySolution(),o.loadSolution(o.best_from_server),o.loadSolution(o.autosavedSolution()),o.problem_is_initialized=!0}function a(){d.innerText="Ошибка при загрузке задачи, попробуйте обновить страницу"}function r(e){d.innerText="Загрузка "+e.progress+"%"}function s(){var e=document.createElement("div");return e.className="loading-info",e}var u=new e(n),d=s();if(t.appendChild(d),u.preloadManifest){var c=new createjs.LoadQueue(!0,i);c.on("complete",o,null,!1,{loading_queue:c}),c.on("error",a),c.on("progress",r);var m=u.preloadManifest();c.loadManifest(m)}else o(null,{loading_queue:null})}function a(e,t){if(t&&t.sol){var n=JSON.parse(t.sol),i=dces2contest.get_problem_index(e);u[i]=n}}Object.defineProperty(t,"__esModule",{value:!0});var r=n(0);t.initializeKioProblem=o;var s=function(){function e(e,t){for(var n=0;n<t.length;n++){var i=t[n];i.enumerable=i.enumerable||!1,i.configurable=!0,"value"in i&&(i.writable=!0),Object.defineProperty(e,i.key,i)}}return function(t,n,i){return n&&e(t.prototype,n),i&&e(t,i),t}}(),l=function(){function e(t,n,o){i(this,e),this.problem=t,this.domNode=n,this.pid=dces2contest.get_problem_index($(n)),this.best_from_server=u[dces2contest.get_problem_index($(n))],this.best=null,this.bestResult=null,this.autosave_localstorage_key="kio-problem-"+this.problem.id()+"-autosave",this.loading_queue=o,this.problem_is_initialized=!1}return s(e,[{key:"saveEmptySolution",value:function(){this.emptySolution=this.problem.solution()}},{key:"loadSolution",value:function(e){null!==e&&this.problem.loadSolution(e)}},{key:"bestSolution",value:function(){return this.best}},{key:"autosavedSolution",value:function(){return JSON.parse(localStorage.getItem(this.autosave_localstorage_key))}},{key:"autosaveSolution",value:function(){this.problem_is_initialized&&localStorage.setItem(this.autosave_localstorage_key,JSON.stringify(this.problem.solution()))}},{key:"newRecord",value:function(e,t){this.problem_is_initialized&&dces2contest.submit_answer(this.pid,{sol:JSON.stringify(e),res:JSON.stringify(t)})}},{key:"submitResult",value:function(e){this.last_submitted_result=e,this.results_info_panel.setParams(e),this.autosaveSolution();var t=this.compareResults(e,this.bestResult);t>0&&(this.bestResult=e,this.best=this.problem.solution(),this.newRecord(this.best,e),this.record_info_panel.setParams(e))}},{key:"compareResults",value:function(e,t){if(null==e&&null==t)return 0;if(null==e)return-1;if(null==t)return 1;var n=this.problem.parameters(),i=!0,o=!1,a=void 0;try{for(var r,s=n[Symbol.iterator]();!(i=(r=s.next()).done);i=!0){var l=r.value,u=e[l.name],d=t[l.name];l.normalize&&(u=l.normalize(u),d=l.normalize(d));var c="maximize"===l.ordering?u-d:d-u;if(Math.abs(c)>1e-9)return c>0?1:-1}}catch(e){o=!0,a=e}finally{try{!i&&s.return&&s.return()}finally{if(o)throw a}}return 0}},{key:"getResource",value:function(e){return this.loading_queue.getResult(e)}},{key:"init_view",value:function(e,t){var i=document.createElement("div"),o=document.createElement("div");i.className="kio-base-box",e.appendChild(i),e.appendChild(o),n.i(r.a)(o,this);var a=$(e).width()-12;t.initialize(i,this,a)}}]),e}();dces2contest.register_solution_loader("kio-online",a);var u=[]}]);