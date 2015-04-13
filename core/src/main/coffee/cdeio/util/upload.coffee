fs = require 'fs'
{mark} = require 'cdeio/mark'
{json, notFound} = require 'cdeio/response'
response = require 'ringo/jsgi/response'
{type} = require 'cdeio/util/type'
{join} = require 'cdeio/util/paths'
{getOptionInProperties} = require 'cdeio/config'
{parseFileUpload, BufferFactory} = require 'ringo/utils/http'

{UUID} = java.util
{String} = java.lang
{Attachment} = com.zyeeda.cdeio.commons.resource.entity


CONFIG_KEY = 'cdeio.upload.path'

exports.mountTo = (router, prefix, args...) ->
    url = if prefix then join('/', prefix) else '/'
    router.post url, createRequestHandler.apply null, args
    router.del join(url, '/:id'), createDeleteHandler()
    router.get join(url, '/:id'), createViewer()

createRequestHandler = exports.createRequestHandler = (path, callbacks...) ->

    if type(path) isnt 'string'
        callbacks.unshift path
        path = null

    prefix = getOptionInProperties CONFIG_KEY

    mark('managers', Attachment).mark('tx').on (am, request) ->
        params = {}
        fileName = UUID.randomUUID().toString()
        now = new Date()
        folder = join path, now.getFullYear(), (now.getMonth() + 1) + '-' + now.getDate()
        filePath = join folder, fileName
        fullPath = join prefix, filePath

        parseFileUpload request, params, null, (data, enc) ->
            return BufferFactory(data, enc) if not data.filename

            fs.makeTree join(prefix, folder)
            fs.touch fullPath
            data.path = filePath
            fs.open fullPath, write: true, binary: true

        throw new Error('Request is not a file upload request') unless params.files
        storeFile = (file) ->
            a = new Attachment()
            a.contentType = file.contentType
            a.path = file.path
            a.filename = file.filename
            am.save a

        if params.files.length is 1
            file = params.files[0]
            result = callbacks[0]?(file, params, request)
            return result if result?.body

            attachment = storeFile(file)
            result = callbacks[1]?(attachment, file, params, request)
            return result if result?.body

            json id: attachment.id, name: attachment.filename

        else if params.files.length > 1
            result = callbacks[0]?(params.file, params, request)
            return result if result?.body

            attachments = (storeFile file for file in params.files)
            result = callbacks[1]?(attachment, params.files, params, request)
            return result if result?.body

            json (id: a.id, name: a.filename for a in attachments)

exports.commitAttachment = mark('managers', Attachment).mark('tx').on (am, ids...) ->
    for id in ids
        attachment = am.find id
        attachment.draft = false


createDeleteHandler = exports.createDeleteHandler = ->

    (req, id) ->
        json id: removeHandler(id)

createViewer = exports.createViewer =  ->

    mark('managers', Attachment).on (am, request, id) ->
        attachment = am.find id
        return notFound 'attchment is not exist' if not attachment

        path = join getOptionInProperties(CONFIG_KEY), attachment.path
        res = response.static path, attachment.contentType

        # 处理文件名乱码问题
        filename = new String(new String(attachment.filename).bytes, 'ISO8859-1');

        # 按上传时的文件名输出文件
        res.headers['Content-Disposition'] = 'attachment;filename=' + filename;
        res

exports.copy = mark('managers', Attachment).mark('tx').on (am, id) ->
    attachment = am.find id
    path = attachment.path
    prefix = path.substring(0, path.lastIndexOf '/')
    newPath = join prefix, UUID.randomUUID().toString()
    prefix = getOptionInProperties CONFIG_KEY
    fs.copy join(prefix, attachment.path), join(prefix, newPath)

    atta = new Attachment()
    atta.filename = attachment.filename
    atta.path = newPath
    atta.contentType = attachment.contentType
    atta.draft = false

    am.save atta
    atta

removeHandler = exports.remove = mark('managers', Attachment).mark('tx').on (am, id) ->
    attachment = am.find id
    fs.remove join(getOptionInProperties(CONFIG_KEY), attachment.path)
    am.remove attachment
    id
