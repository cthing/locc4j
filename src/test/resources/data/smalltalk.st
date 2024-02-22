Object subclass: #Curl.

Curl comment:
  'A basic Curl unix wrapper'.

Curl class extend [
  new [ ^(super new) init. ]

  "Constants"
  OPT_FOLLOWLOCATION  [ ^'--location'. ]
  OPT_VERSION         [ ^'--version'. ]
  METHOD_GET          [ ^'GET'. ]
  METHOD_POST         [ ^'POST'. ]
  METHOD_PUT          [ ^'PUT'. ]
  METHOD_DELETE       [ ^'DELETE'. ]
  METHOD_HEAD         [ ^'HEAD'. ]
  METHOD_OPTIONS      [ ^'OPTIONS'. ]


  version [
    Curl exec: Curl OPT_VERSION.
  ]

  exec: cmd [
    Smalltalk system: 'curl ', cmd.
  ]

  request: aUrl [|c|
    c := self new.
    c opt: Curl OPT_FOLLOWLOCATION value: true.
    c url: aUrl.
    ^c request.
  ]
]

Curl extend [
  "Instance variables"
  |host url uri data query port protocol opts|

  "
    Getter for the host instance variable

    @message host
    @return host {String}
  "
  host [ ^host. ]

  "
    Getter for the host instance variable

    @message url
    @return url {String}
  "
  url [ ^url. ]

  "
    Getter for the uri instance variable

    @message uri
    @return uri {String}
  "
  uri [ ^uri. ]

  "
    Getter for the data instance variable

    @message data
    @return data {Dictionary}
  "
  data [ ^data. ]

  "
    Getter for the query instance variable

    @message query
    @return query {String}
  "
  query [ ^query. ]

  "
    Getter for the port instance variable

    @message port
    @return port {Integer}
  "
  port [ ^port. ]

  "
    Getter for the protocol instance variable

    @message protocol
    @return protocol {String}
  "
  protocol [ ^protocol. ]

  "
    Getter for the opts instance variable

    @message opts
    @return opts {Dictionary}
  "
  opts [ ^opts. ]

  "
    Getter for the host instance variable

    @message host
    @return host {String}
  "
  host [ ^host. ]

  "
    Initializes the Curl instance object

    @message init
    @return self
  "
  init [
    host      := ''.
    url       := ''.
    uri       := ''.
    query     := '?'.
    port      := 0.
    protocol  := 'http'.
    opts      := Dictionary new.
    data      := Dictionary new.

    ^self.
  ]

  "
    Sets a curl option and value if applicable

    @message setOpt
    @param opt - Option to set
    @param value - Value of the option, if the value is 'true' then the value is not appended
    @return self
  "
  opt: opt value: value [
    opts at: opt put: value.

    ^self.
  ]

  "
    Setter for the port

    @message port:
    @param aPort {Integer} - A port number. If the value is 0 or 80 then it will be ignored when building the request
    @return self
  "
  port: aPort [
    port := aPort.
    ^self.
  ]

  "
    Setter for the query

    @message query:
    @param key {String} - A query variable
    @param value {String} - A query variable value
    @return self
  "
  query: key value: value [
    (query == nil) ifTrue: [
      query := key.
      (value size > 0) ifTrue: [
        query := query, '=', value.
      ]
    ] ifFalse: [
      (query size == 1) ifTrue: [
        query := query, key, '=', value.
      ] ifFalse: [
        query := query, '&', key, '=', value.
      ]
    ].

    ^self.
  ]

  "
    Setter for the uri

    @message uri:
    @param aUri {String} - URI segment of the URL
    @return self
  "
  uri: aUri [
    uri := aUri.
    ^self.
  ]

  "
    Setter for the URL

    @message url:
    @param aUrl {String} - A URL override
    @return self
  "
  url: aUrl [
    url := aUrl.
    ^self.
  ]

  "
    Setter for the host

    @message host:
    @param aHost {String} - The host portion of the URL
    @return self
  "
  host: aHost [
    host := aHost.
    ^self.
  ]

  "
    Sends a GET request

    @message GET:
    @return self
  "
  GET [
    self request: Curl METHOD_GET.
    ^self.
  ]

  "
    Sends a POST request

    @message POST:
    @return self
  "
  POST [
    self request: Curl METHOD_POST.
    ^self.
  ]

  "
    Sends a PUT request

    @message PUT:
    @return self
  "
  PUT [
    self request: Curl METHOD_PUT.
    ^self.
  ]

  "
    Sends a DELETE request

    @message DELETE:
    @return self
  "
  DELETE [
    self request: Curl METHOD_DELETE.
    ^self.
  ]

  "
    Sends the request based on a method type

    @message request
    @param type - The type of method request [GET, POST, DELETE, PUT, HEAD, OPTIONS]
    @return self
  "
  request: type [
    "Does the URL currently have a length?"
    (url size > 0) ifFalse: [
      "Set the protocol and host parts"
      url := protocol, '://', host.

      "Port 80 or 0 set?"
      (port ~= 80 & port > 0) ifTrue: [
        "Attach the port part of the URL"
        url := url, ':', port asString.
      ].

      (uri size > 0) ifTrue: [
        "Attach the URI if given"
        url := url, '/', uri.
      ].

      (query size > 1) ifTrue: [
        "Attach the query part if given"
        url := url, query.
      ].
    ].

    "Iterate over each option by key => value and build the opts string"
    opts keysAndValuesDo: [:opt :value |
      (opt size > 0) ifTrue: [
        url := url, ' ', opt.
        (value size > 0 & value ~= true) ifTrue: [
          url := url, ' ', value.
        ]
      ]
    ].

    "Attach the HTTP method type part"
    url := url, ' -X ', type.

    "Execute the request"
    Curl exec: url.

    ^self.
  ]

  "
    Sends a GET request by default

    @see Curl#request
  "
  request [
    ^self request: Curl METHOD_GET.
  ]
]
