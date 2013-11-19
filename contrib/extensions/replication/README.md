==================================
sling.replication
==================================

How it works
------------

 - Replication agents
    - Each replication agent is a proper OSGi service
    - Replication agents are resolved using a Sling Resource Provider who locate them under `/system/replication/agent`
    - Replication agents replication can be triggered by sending `HTTP POST` requests to `http://$host:$port/system/replication/agent/$agentname` with HTTP parameters `X-replication-action` and `X-replication-path`
    - Replication agents configuration 
        - Replication agents' configurations are proper OSGi configurations (backed nodes of type `sling:OsgiConfig` in the repository), see [ReplicationAgentServiceFactory-publish.json](src/main/resources/SLING-CONTENT/libs/sling/replication/agents/org.apache.sling.replication.agent.impl.ReplicationAgentServiceFactory-publish.json)
        - Replication agents configuration include:
            - Transport handlers: currently only basic HTTP is supported
            - Package builders: currently only FileVault packages are supported
            - Queue types: current implementations are:
                - In memory
                - Sling Job Handling based
            - Endpoint: URI of the resource to replicate to
            - Name: the name of the agent
            - Authentication handlers: currently supported user/password and `nop` authentication handlers for HTTP transport
            - Queue distribution: how items (packages) to be replicated are distributed to agent's queues 
        - Replication agents' configurations can be retrieved via `HTTP GET`
            - `http -a admin:admin -v -f GET http://localhost:8080/system/replication/agent/publish/configuration`
        - Replication agents' configurations can be updated via `HTTP POST`
            - `http -a admin:admin -v -f POST http://localhost:8080/system/replication/agent/publish/configuration enpoint=newendpoint`
 - Replication queues
    - In Memory queue
        - draft implementation using an in memory blocking queue together with a Sling scheduled processor which periodically fetches the first item of each queue and trigger a replication of such an item
        - not suitable for production as it's currently not persisted and therefore restarting the bundle / platform would remove the queue
    - Sling Job Handling based queue
        - each queue addition triggers a Sling job creation
        - requires the creation of a Sling JobConsumer for the replication agent when a replication agent is created / updated
        - by default Sling queues for replication are
            - ordered
            - with max priority
            - with infinite retries
            - keeping job history
    - Distribution of packages among queues:
        - a replication agent can be configured to use a specific queue distribution mechanism
        - the queue distribution strategy defines how packages are routed into agent's queues
        - current distribution strategies:
            - single: the agent has one only queue and all the items are routed there
            - priority path: the agent can route a configurable set of paths (note that this configuration is global for the system, not per agent) to a dedicated priority queue while all the others go to the default queue
            - error aware: the agent has one default queue for all the items, items failing for a configurable amount of times are either dropped or moved to an error queue  
 - Direct replication use case
    - User makes an `HTTP POST`request to `http://localhost:8080/system/replication/agent/publish` with parameter `X-replication-action=ACTIVATE` and `X-replication-path=/content`
    - `ReplicationAgentServlet` servlet is triggered
    - `ReplicationAgentServlet` servlet provides the resource to a `ReplicationAgent` by `ReplicationAgentResourceProvider`
    - Replication agent resource provider gets the OSGi service with name `publish`
    - `ReplicationAgent` executes the replication request (activate the node at path /content)
    - `ReplicationAgent` get the status of the request and update the response accordingly
    - `ReplicationAgentServlet` maps the agent response to an HTTP status accordingly
  

How to use it
--------------
- install the dependencies' bundles on author and publish
- install this bundle on author and publish
- create some content on author (e.g. /content/sample1)
- activate by sending an HTTP POST on author: 
```http -a admin:admin -v -f POST http://localhost:8080/system/replication/agent/publish X-replication-action=ACTIVATE X-replication-path=/content```
- deactivate by sending an HTTP POST on author: 
```http -a admin:admin -v -f POST http://localhost:8080/system/replication/agent/publish X-replication-action=DEACTIVATE X-replication-path=/content```

Tasks
------------------------

- [x] File Vault based serialization
- [x] adapter for creating replication content
- [x] OSGi service for agent
- [x] OSGi configurations for agents
- [x] Resource provider for mapping replication requests to agents
- [x] in memory queue
- [x] basic HTTP transport handler
- [x] Sling job handling based queue
- [x] HTTP api for getting and updating agent configuration
- [x] HTTP api for posting replication requests
- [x] add pluggable queue distribution strategy
- [] add failure handlers
- [] HTTP api for getting agent / queue status
- [] agent admin api, works with sling resource api
- [] user replication api or at least a packed and consistent way of doing that
- [] replication rules: developed some utils for defining accepted rules
- [] distributed configuration
- [] transport handler pushing to jms
- [] add authentication handlers
- [] add distribution handlers
- [] websocket transport handler
- [] websocket for reverse replication control channel (avoids author to pull, enables publish to push)
- [] cluster aware transport handler with list of publish instances (e.g. use topology api for reverse replication to do the polling in clustered scenarios)
- [] Implement Reverse Replication for Users + Profiles