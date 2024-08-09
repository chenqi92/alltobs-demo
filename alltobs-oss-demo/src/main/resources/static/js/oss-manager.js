new Vue({
    el: '#app',
    data: {
        newBucketName: '',
        selectedBucket: '',
        objectPrefix: '',
        buckets: [],
        objects: []
    },
    methods: {
        fetchBuckets() {
            fetch('/oss/buckets')
                .then(response => response.json())
                .then(data => {
                    this.buckets = data.map(name => ({name}));
                });
        },
        createBucket() {
            fetch(`/oss/bucket?bucketName=${this.newBucketName}`, {method: 'POST'})
                .then(() => this.fetchBuckets());
        },
        deleteBucket(name) {
            fetch(`/oss/bucket?bucketName=${name}`, {method: 'DELETE'})
                .then(() => this.fetchBuckets());
        },
        listObjects() {
            fetch(`/oss/objects?bucketName=${this.selectedBucket}&prefix=${this.objectPrefix}`)
                .then(response => response.json())
                .then(data => {
                    this.objects = data;
                });
        },
        deleteObject(name) {
            fetch(`/oss/object?bucketName=${this.selectedBucket}&objectName=${name}`, {method: 'DELETE'})
                .then(() => this.listObjects());
        },
        downloadObject(name) {
            fetch(`/oss/download?bucketName=${this.selectedBucket}&objectName=${name}`)
                .then(response => response.text())
                .then(url => {
                    window.open(url, '_blank');
                });
        }
    },
    mounted() {
        this.fetchBuckets();
    }
});
