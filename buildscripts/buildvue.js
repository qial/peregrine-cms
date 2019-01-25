const process   = require('process')
const fs        = require('fs-extra')

const rollup    = require( 'rollup' )
const path      = require('path')
const vue       = require('rollup-plugin-vue')
const buble     = require('rollup-plugin-buble')
const camelcase = require('camelcase')

console.log('=== building vue files ========================================')

var felibName = ''
if(process.argv.length >= 3) {
    felibName = process.argv[2]
} else {
    console.log('please provide a name for the felib to use')
    process.exit(-1)
}

console.log('building felib', felibName)

var basePath = './src/main/content/jcr_root/apps'
var distBasePath = './target/classes/etc/felibs/'+felibName

/** creatte the target directories
 *
 */
fs.mkdirsSync(distBasePath)
fs.mkdirsSync(distBasePath+'/css')
fs.mkdirsSync(distBasePath+'/js')

/**
 * compile a single file
 *
 * @param file
 * @returns {{name: string, nameCamelCase: *, nameCapitalCamelCase: string}}
 */
function compileComponent(file){

    compiling.push(file)

    console.log("compiling vue file: .%s", file)
  // get component name from file name (file: a.vue, name: a)
  var name = file.substring(1, file.lastIndexOf('/')).toLowerCase().split('/').join('-')
  var nameCamelCase = camelcase(name)
  var nameCapitalCamelCase = nameCamelCase.charAt(0).toUpperCase() + nameCamelCase.slice(1)

  // each component needs a unique module name
  var moduleName = 'cmp'+nameCapitalCamelCase

  // compile the Vue component and give us a .js and .css
  rollup.rollup({
    input: `${basePath}${file}`,
    plugins: [
      vue({
        compileTemplate: true,
        css: `${distBasePath}/css/${nameCamelCase}.css`
      }),
      buble()
    ]
  }).then( function(bundle) {

    bundle.write({
      format: 'iife',
      name: moduleName,
      file:`${distBasePath}/js/${nameCamelCase}.js`,
      globals: {
        tools: 'tools',
          log: 'log'
      }

    }).then( function() {
        const index = compiling.indexOf(file)
        if(index >= 0) {
            compiling.splice(index, 1)
        }
        updateIndexFiles()
    })
  }).catch( (error) => {
      const index = compiling.indexOf(file)
      if(index >= 0) {
          compiling.splice(index, 1)
      }
      console.error(error)
  })
  return { name: name, nameCamelCase: nameCamelCase, nameCapitalCamelCase: nameCapitalCamelCase}
}

/**
 * rewrites the js.txt and css.txt file used to combine the css and js file in the frontend
 *
 */
function updateIndexFiles() {
    var jsFiles = readDirs(distBasePath+'/', distBasePath + '/js', '.js')
    jsFiles.unshift('<!-- auto generated by build -->')
    fs.writeFileSync(distBasePath+'/js.txt', jsFiles.join('\n'))

    var cssFiles = readDirs(distBasePath+'/', distBasePath + '/css', '.css')
    cssFiles.unshift('<!-- auto generated by build -->')
    fs.writeFileSync(distBasePath+'/css.txt', cssFiles.join('\n'))

    if(compiling.length === 0) {
        console.log('done compiling')

        if(process.argv[3] === 'upload') {
            console.log('upload')
            const slang = require('./slang.js')
            slang.setOptions({
                port: 8080,
                host: 'localhost'
            })

            const appName = felibName
            console.log('felib name is', appName)

            process.chdir('target/classes')
            slang.up('etc/felibs/' + appName + '/js.txt').then(function (status) {
//                console.log('success', status)
            }).catch(function (status, err) {
                console.error('error', status, err)
            });
            slang.up('etc/felibs/' + appName + '/css.txt').then(function (status) {
//                console.log('success', status)
            }).catch(function (status, err) {
                console.error('error', status, err)
            });

            jsFiles.slice(1).forEach( function(file) {
                slang.up('etc/felibs/' + appName + '/'+file).then(function (status) {
//                    console.log('success', status)
                }).catch(function (status, err) {
                    console.error('error', status, err)
                });
            })
            cssFiles.slice(1).forEach( function(file) {
                slang.up('etc/felibs/' + appName + '/'+file).then(function (status) {
//                    console.log('success', status)
                }).catch(function (status, err) {
                    console.error('error', status, err)
                });
            })
            //     slang.up('etc/felibs/'+appName+'/js/'+appName+'Components'+componentName.charAt(0).toUpperCase()+componentName.substring(1)+'.js').then(function(status) {
            //         console.log('success',status)
            //     }).catch(function(status, err) {
            //         console.error('error', status, err)
            //     });
            // }
        }
    }
}

/**
 *
 * read a directory and its children and return all the files with the given extension
 *
 * @param basePath
 * @param path
 * @param extFilter
 * @returns {Array}
 */
function readDirs(basePath, path, extFilter) {
    var ret = new Array()
    var files = fs.readdirSync(path)
    files.forEach( function(file) {
        var filePath = path + '/' + file;
        if(filePath.endsWith(extFilter)) {
            ret.push(filePath.slice(basePath.length))
        }
        var stats = fs.statSync(filePath)
        if(stats.isDirectory()) {
            ret = ret.concat(readDirs(basePath, filePath, extFilter))
        }
    })
    return ret;
}

// find all the vue files in this project
var vueFiles = readDirs(basePath, basePath, '.vue')
var compiling = []
// for each of the files compile it
for(let i = 0; i < vueFiles.length; i++) {
    compileComponent(vueFiles[i])
}



